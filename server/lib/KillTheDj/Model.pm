# Kill The DJ is a social jukebox for Spotify and Android
#   
# Copyright (C) 2012, Warp Networks, S.L
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
package KillTheDj::Model;

use Redis;
use Mojo::Log;
use Data::Dumper;
use MIME::Base64 qw(decode_base64);

use constant CLIENT_TRACK_FIELDS => qw/link title artist album/;

# We allow to add 3 tracks per 60 seconds
use constant ALLOWED_TRACKS_TO_ADD   => 3;
use constant TIME_BETWEEN_ADD_TRACKS => 60;

my $r = Redis->new(reconnect => 60);
my $log = Mojo::Log->new();

sub set_redis {
  my ($redis) = @_;
  $r = $redis;
}

use LWP::Simple;

sub valid_track {
  my ($link) = @_;
  my $url = "http://ws.spotify.com/lookup/1/.json?uri=" . $link;
  $log->debug("Valid track? " . $url);
  return scalar head($url);
}

sub add_track {
  my ($party, $user_id, $track) = @_;
  return ("invalid", -1) unless valid_track($track->{track}->{link});
  $log->debug("User_id: $user_id");
  my $tracks_added = $r->llen("$party:add_track:$user_id");
  $log->debug("This user has added lately $tracks_added tracks");
  if ($tracks_added < ALLOWED_TRACKS_TO_ADD) {
    $r->rpush("$party:add_track:$user_id", time());
  } else {
    $log->debug("Which are more than ". ALLOWED_TRACKS_TO_ADD);
    $added_time = $r->lpop("$party:add_track:$user_id");
    $log->debug("$added_time is the time to compare: ".time());
    if ((time() - $added_time) > TIME_BETWEEN_ADD_TRACKS) {
      # allowed
      $r->rpush("$party:add_track:$user_id", time());
    } else {
      # not allowed
      $r->lpush("$party:add_track:$user_id", $added_time);
      return ("not_allowed", -1);
    }
  }
  # TODO
  #  - Check already in playlist
  #  - Check played recently
  #  - Other checks?

  my $id = $r->incr("$party:requests");
  $log->debug("Adding track $id " . Dumper($track));
  for my $key (CLIENT_TRACK_FIELDS) {
    my $value = $track->{track}->{$key};
    $r->set("$party:request:$id:$key" => $value);
  }
  $r->set("$party:request:$id:status" => "added");
  $r->set("$party:request:$id:time" => time());
  $r->set("$party:vote:$user_id:$id" => "up");
  $r->zadd("$party:queue", 1 => "$id");
  return ("added", $id);
}

sub current_track {
  my ($party) = @_;
  my $id = $r->get("$party:request:current");
  return &_fetch_track($party, $id) if $id;
}

sub current_cover {
  my ($party, $cover) = @_;
  $log->debug("Saving data: ".length($cover)." bytes");
  open(F, ">public/covers/$party.png");
  print F decode_base64($cover);
  close(F);
  return true;
}
sub _fetch_track {
  my ($party, $id, $user_id) = @_;
  my $track = { votes => $r->zscore("$party:queue", $id), id => $id };
  for my $field (CLIENT_TRACK_FIELDS, "time", "status") {
     $track->{$field} = $r->get("$party:request:$id:$field");
  }
  $track->{vote} = $r->get("$party:vote:$user_id:$id") if $user_id;
  return $track;
}

sub queue {
  my ($party, $user_id) = @_;
  $log->debug("Retrieving queue");
  my @queue = $r->zrevrange("$party:queue", 0, -1);
  my @tracks;
  for my $id (@queue) {
    push(@tracks, _fetch_track($party, $id, $user_id));
  }
  @tracks = sort {
      if ($a->{votes} == $b->{votes}) {
        $a->{time} <=> $b->{time};
      } else {
        $b->{votes} <=> $a->{votes};
      }
    } @tracks;
  return \@tracks;
}

sub vote {
  my ($party, $user_id, $vote) = @_;
  my $id = $vote->{vote}->{id};
  my $type = $vote->{vote}->{type};
  # TODO Check track
  my $incr = $type eq 'up' ? 1 : -1;
  $log->debug("Voting $type for $id");
  my $current_vote = $r->get("$party:vote:$user_id:$id");
  if ($current_vote) {
    $log->debug("We had voted before");
    my $incr2 = $current_vote eq 'up' ? -1 : 1;
    $r->zincrby("$party:queue", $incr2, "$id");
  }
  $r->set("$party:vote:$user_id:$id" => $type);
  return $r->zincrby("$party:queue", $incr, "$id");
}

sub played {
  my ($party, $played) = @_;
  my $id = $played->{played}->{id};
  $r->zrem("$party:queue", $id);
  $r->set("$party:request:$id:status", "played");
  $r->set("$party:request:current" => $id);
  $log->debug("Removing $id from queue");
}

sub removed {
  my ($party, $removed) = @_;
  my $id = $removed->{removed}->{id};
  $r->zrem("$party:queue", $id);
  $r->set("$party:request:$id:status", "removed");
  $log->debug("Removing $id from queue");
}

sub status {
  my ($party, $ids, $user_id) = @_;
  my @tracks;
  my @queue = @{queue($party, $user_id)};
  foreach $id (split(',', $ids)) {
    my $track = _fetch_track($party, $id, $user_id);
    if ($track->{status} eq "added") {
      my ($index) = grep { $queue[$_]->{id} eq $track->{id} } 0..$#queue;
      $track->{position} = $index + 1;
    }
    push(@tracks, $track);
  }
  return \@tracks;
}

1;

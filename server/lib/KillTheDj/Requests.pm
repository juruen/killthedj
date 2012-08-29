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
package KillTheDj::Requests;

use Mojo::Base 'Mojolicious::Controller';
use KillTheDj::Model;

# Methods with limit requests over time --------------------------------------

# Add track to the queue
sub add_track {
  my $self = shift;
  my $user_id = $self->req->headers->header("android-device-id");
  my ($status, $id) = KillTheDj::Model::add_track($self->param("party"), $user_id, $self->req->json);
  if ($status eq "not_allowed") {
    $self->render_json({status => $status}, status => 403);
  } elsif ($status eq "added") {
    $self->render_json({status => $status, id => $id}, status => 200);
  } else {
    $self->render_json({status => $status, id => $id}, status => 400);
  }
}

# Vote some track from the queue
sub vote {
  my $self = shift;
  my $user_id = $self->req->headers->header("android-device-id");
  my $votes = KillTheDj::Model::vote($self->param("party"), $user_id, $self->req->json);
  $self->render_json({votes => $votes});
}

# Methods getting info, without limitations ----------------------------------

# Clients get info about current track
sub current_track {
  my $self = shift;
  my $track = KillTheDj::Model::current_track($self->param("party"));
  if ($track) {
    $self->render_json($track);
  } else {
    $self->rendered(404);
  }
}

# Clients get info about a subset of tracks
sub status {
  my $self = shift;
  my $user_id = $self->req->headers->header("android-device-id");
  $self->render_json(KillTheDj::Model::status($self->param("party"), $self->param("ids"), $user_id));
}

# Both Spotify and clients get the queue of tracks
sub queue {
  my $self = shift;
  my $user_id = $self->req->headers->header("android-device-id");
  $self->render_json(KillTheDj::Model::queue($self->param("party"), $user_id));
}

# Spotify client sending cover to us
sub current_cover {
  my $self = shift;
  my $ret = KillTheDj::Model::current_cover($self->param("party"), $self->req->body_params->param('cover'));
  $self->rendered($ret ? 201 : 500);
}

# Spotify client says this song is being played right now
sub played {
  my $self = shift;
  my $votes = KillTheDj::Model::played($self->param("party"), $self->req->json);
  $self->render_json({});
}

# Spotify client bans some track
sub removed {
  my $self = shift;
  my $votes = KillTheDj::Model::removed($self->param("party"), $self->req->json);
  $self->render_json({});
}

1;

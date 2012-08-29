#!/usr/bin/env perl
use Mojo::Base -strict;

use Test::More tests => 48;
use Test::Mojo;

use Redis;
use JSON;

use constant TEST_DB => 1;

use_ok "KillTheDj";
use_ok "KillTheDj::Model";

my ($r, $t);

sub json_track {
  return
    sprintf (
      '{"track":{"title":"%s","album":"%s","artist":"%s","link":"%s"}}',
      @_
  );
}

sub track_desc
{
  my $id = $_[0];
  return ("title$id", "album$id", "artist$id", "link$id");
}

sub perl_track {
  return
  {
    title => $_[0],
    album => $_[1],
    artist => $_[2],
    link => $_[3],
    id => $_[4],
    votes => $_[5],
  };
}

sub track {
  my $id = $_[0];
  my $votes = $_[1];
  $votes = 1 if (not defined $_[1]);
  return (perl_track(track_desc($id), $id, $votes));
}


sub setup_tests {
  $t = Test::Mojo->new("KillTheDj");
  $r = Redis->new();
  $r->select(TEST_DB);
  $r->flushdb;
  KillTheDj::Model::set_redis($r);
}

setup_tests();
my $party = "scientific";
# Test Queue is empty
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_content_is([]);

# Test we can add a track
$t->post_ok("/api/v1/$party/track" => json_track(track_desc(1)));
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(1)]);

# Test we can vote for a track
# Vote up
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 1, type => "up" }}));
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(1, 2)]);
# Vote up once again
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 1, type => "up" }}));
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(1, 3)]);
# Vote down
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 1, type => "down" }}));
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(1, 2)]);
# Vote down
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 1, type => "down" }}));
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(1, 1)]);

# Add second track
$t->post_ok("/api/v1/$party/track" => json_track(track_desc(2)));

# Vote second one up
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 2, type => "up" }}));
# Check order
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(2, 2), track(1,1)]);

# Add third track
$t->post_ok("/api/v1/$party/track" => json_track(track_desc(3)));
# Vote it up twice
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 3, type => "up" }}));
$t->post_ok("/api/v1/$party/vote" => JSON::encode_json({vote => { id => 3, type => "up" }}));
# Check order
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(3,3), track(2, 2), track(1,1)]);

# Play third track
$t->post_ok("/api/v1/$party/played" => JSON::encode_json({ played => { id => 3 } }));
# Current status should be the last played
$t->get_ok("/api/v1/$party/current")->status_is(200)->json_has(track(3,3));
# Check track is not there any longer
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(2, 2), track(1,1)]);

# Remove one track
$t->post_ok("/api/v1/$party/removed" => JSON::encode_json({ removed => {id => 2} }))->status_is(200);
$t->get_ok("/api/v1/$party/queue")->status_is(200)->json_has([track(1,1)]);

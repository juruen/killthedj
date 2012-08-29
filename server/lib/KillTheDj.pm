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
package KillTheDj;
use Mojo::Base 'Mojolicious';

# This method will run once at server start
sub startup {
  my $self = shift;

  # Documentation browser under "/perldoc"
  $self->plugin('PODRenderer');

  # Routes
  my $r = $self->routes;

  # Normal route to controller
  $r->route('/api/v1/:party/track')->to('requests#add_track');
  $r->route('/api/v1/:party/current')->to('requests#current_track');
  $r->route('/api/v1/:party/current_cover')->to('requests#current_cover');
  $r->route('/api/v1/:party/queue')->to('requests#queue');
  $r->route('/api/v1/:party/vote')->to('requests#vote');
  $r->route('/api/v1/:party/played')->to('requests#played');
  $r->route('/api/v1/:party/status/:ids')->to('requests#status');
  $r->route('/api/v1/:party/status')->to('requests#status', ids => "");
  $r->route('/api/v1/:party/removed')->to('requests#removed');
}

1;

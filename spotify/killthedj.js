/**
* Kill The DJ is a social jukebox for Spotify and Android
*   
* Copyright (C) 2012, Warp Networks, S.L
* 
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*
*/
var killthedj = ( function() {
    /* Spotify related vars */
    var sp = getSpotifyApi(1);
    var models = sp.require('sp://import/scripts/api/models');
    var views = sp.require('sp://import/scripts/api/views');
    var player = models.player;

    var updater;
    var updater_running;
    var party;
    var queue_hash = {};
    var next_id;
    var current_id;
    var play = false;
    var first_time = true;
    var alreadyBinded = false;
    var server = "http://192.168.45.59:3000";

    /* Update page header with the track currently playing */
    function updatePageWithTrackDetails() {
        var header = document.getElementById("header");
        // This will be null if nothing is playing.
        var playerTrackInfo = player.track;
        if (playerTrackInfo == null) {
            header.innerText = "Nothing playing!";
        } else {
            var track = playerTrackInfo.data;
            header.innerHTML = "<strong>"+track.name+"</strong> <br/>from <em>" + track.album.name
                + "</em> by <em>" + track.album.artist.name + "</em>.";
        }
    }

    function url_for(method) {
      return server + '/api/v1/' + party + '/' + method;
    }

    function updateQueue() {
      if (updater_running == 0) return;
      $.get(
        url_for('queue'),
        {},
        function (data) {
          var needToStartPlay = false;
          if ((next_id == undefined || !player.playing) && play) {
            needToStartPlay = true;
          }
          next_id = undefined;
          var trackList = '<div class="sp-list" tabindex="0"><div style="height:' + data.length * 20 + 'px;">';
          data.forEach(function(item, i) {
            var track_info = {
                "title" : item["title"],
                "album" : item["album"],
                "artist" : item["artist"],
                "votes" : item["votes"],
                "id" : item["id"],
                "link" : item["link"],
            };
            if (next_id == undefined) {
              next_id = track_info["id"];
            }
            queue_hash[track_info["id"]] = track_info;
            trackList += '<a href="#" class="sp-item sp-track sp-track-availability-0" title="' +
              track_info["title"] + '" data-itemindex="' + i + '" data-viewindex="' + i + '" style="-webkit-transform: translateY(' +
              i * 20 + 'px); ">' +
                '<span class="position">' + (i+1) + '</span>' +
                '<span class="sp-track-field-name">' + track_info["title"] + '</span>' +
                '<span class="sp-track-artist-name">' + track_info["artist"] + '</span>' +
                '<span class="sp-track-album-name">' + track_info["album"] + '</span>' +
                '<span class="votes">' + track_info["votes"] + '</span>' +
                '<span class="delete"><img height="10px" src="sp://killthedj/delete.png" class="delete" id="delete_'+track_info['id']+'"/></span>' +
              "</a>";

          });
          trackList += "</div>";
          $('#queue').html(trackList);
          if (needToStartPlay) {
            startPlaying();
          }
        },
        'json'
      ).error(function() {
        $('#queue').html("Error trying to fetch queue from server :(");
      });
      startQueueUpdater();
    }

    function startQueueUpdater() {
      if (updater_running == 0) {
        return;
      }
      updater = window.setTimeout(function () { updateQueue(); }, 10000);
    }

    function stopQueueUpdater() {
      window.clearTimeout(updater);
      updater_running = 0;
    }

    function onStartClick() {
        if (play) {
            return;
        }
        play = true;
        startPlaying();
        $('#start-button').attr("disabled", "disabled");
        $('#stop-button').removeAttr("disabled");
    }

    function onStopClick() {
        player.ignore();
        if (!play) {
            return;
        }
        play = false;
        player.playing = false;
        $('#stop-button').attr("disabled", "disabled");
        $('#start-button').removeAttr("disabled");
    }

    function sendCoverData() {
        var img = document.getElementById('img-cover');
        // Create an empty canvas element
        var canvas = document.createElement("canvas");
        canvas.width = img.width;
        canvas.height = img.height;

        // Copy the image contents to the canvas
        var ctx = canvas.getContext("2d");
        ctx.drawImage(img, 0, 0);

        var dataURL = canvas.toDataURL("image/png");
        var imgBase64 = dataURL.replace(/^data:image\/(png|jpg);base64,/, "");

        console.info("Sending image");
        $.post(url_for('current_cover'), {'cover': imgBase64});
    }

    function showCover(trackUri) {
        models.Track.fromURI(trackUri, function(track) {
            var album = track.album;
            var img = document.getElementById('img-cover');
            img.src = album.data.cover;
        });
    }

    /* Play track and update variables accordingly */
    function playNextTrack() {
        if (next_id == undefined) {
            console.log("next id not set");
            return;
        }
        var link = queue_hash[next_id]["link"];
        console.log("Id (" + next_id + ") Link (" + link + ") to be played");
        player.play(link);
        showCover(link);
        notifyPlayed(next_id);
        current_id = next_id;
    }

    function subscribePlayerEvents() {
        console.log("Subscribe to player events");
        player.ignore();
        player.observe(models.EVENT.CHANGE, function (e) {
            // Only update the page if the track changed
            console.log("Player event");
            console.log(e);
            if (e.data.curtrack == true) {
                if (player.playing && current_id != next_id) {
                    playNextTrack();
                }
                updatePageWithTrackDetails();
            }
            if (play && !player.playing) {
                console.log("player not playing");
                if (current_id == next_id) {
                    console.log("Current id = next id");
                } else {
                    playNextTrack();
                }
            }
        });
    }

    function startPlaying() {
        playNextTrack();
        subscribePlayerEvents();
    }

    function notifyPlayed(id) {
        // TODO auth?
        $.ajax({
            type: 'POST',
            url: url_for('played'),
            data: JSON.stringify({ "played": { "id": id }}),
            dataType: 'json'
        });
    }

    function deleteTrack(e) {
        var id = e.id.match(/delete_(\d+)/)[1];
        e.src = "sp://killthedj/loading.gif";
        e.disabled = true;
        console.info("Delete on " + id);
        // TODO auth?
        $.ajax({
            type: 'POST',
            url: url_for('removed'),
            data: JSON.stringify({ "removed": { "id": id }}),
            dataType: 'json'
        });
        stopQueueUpdater();
        updater_running = 1;
        updateQueue();
    }

    function bindEvents() {
        if (alreadyBinded) return;
        alreadyBinded = true;
        $('#start-button').click(function(){ onStartClick() });
        $('#stop-button').click(function(){ onStopClick() }).attr("disabled", "disabled");
        $('img.delete').live('click', function(){ deleteTrack(this); });
        $('#img-cover').load(function(){ sendCoverData(); });
    }

    return {
        init: function(party_id) {
            updatePageWithTrackDetails();
            updater_running = 1;
            party = party_id;
            startQueueUpdater();
            bindEvents();
            console.log("Init done");
        },
        stop: function() {
            onStopClick();
            stopQueueUpdater();
            console.log("stopped");
        }
    };

})();


function start_party() {
    var party_id = $('#party-id').val();
    if (party_id && party_id.length) {
        $('#start').hide();
        $('#party').show();
        killthedj.init(party_id);
    }
}

function back_start() {
    $('#party').hide();
    $('#start').show();
    killthedj.stop();
}

window.onload = function() {
    $('button.party').click(start_party);
    $('input.party').keypress(function(e){
        if (e.which == 13) start_party();
    });
    $('#back-start').click(back_start);
}


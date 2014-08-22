/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

@Controller
public class VideoSvc{

	public static final String DATA_PARAMETER = "data";

	public static final String ID_PARAMETER = "id";

	public static final String VIDEO_SVC_PATH = "/video";
	
	public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
	
	private static final AtomicLong currentId = new AtomicLong(0L);

	    private Map<Long,Video> videos = new HashMap<Long, Video>();
	    private Map<Long, MultipartFile> videosDB= new HashMap<Long, MultipartFile>();

	    public Video save(Video entity) {
	        checkAndSetId(entity);
	        videos.put(entity.getId(), entity);
	        return entity;
	    }

	    private void checkAndSetId(Video entity) {
	        if(entity.getId() == 0){
	            entity.setId(currentId.incrementAndGet());
	        }
	    }
	    
	    private String getDataUrl(long videoId){
            String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
            return url;
        }

        private String getUrlBaseForLocalServer() {
           HttpServletRequest request = 
               ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
           String base = 
              "http://"+request.getServerName() 
              + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
           return base;
        }
        
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		// TODO Auto-generated method stub
		return videos.values();
	}

	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		checkAndSetId(v);
		v.setDataUrl(getDataUrl(v.getId()));
		return save(v);
	}


	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> setVideoData(@PathVariable("id") long id, @RequestParam("data") MultipartFile videoData) {
		System.out.println("id received is " + id);
		// TODO Auto-generated method stub
		VideoStatus status = new VideoStatus(VideoState.READY);
		Video v = videos.get(id);
		//v.setContentType(videoData);
		if (v != null) {
			videosDB.put(id, videoData);
			return new ResponseEntity<String>(status, HttpStatus.OK);
		}
		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	}
	
	
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.GET)
    public @ResponseBody MultipartFile getVideoData(@PathVariable("id") long id) {
		if (videosDB.get(id) != null) {
			MultipartFile videoData = videosDB.get(id);
			return videoData;
		}
		return null;
	}
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */

	
}

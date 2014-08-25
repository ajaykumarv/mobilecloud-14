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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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


@Controller
public class VideoSvc{

	public static final String DATA_PARAMETER = "data";

	public static final String ID_PARAMETER = "id";

	public static final String VIDEO_SVC_PATH = "/video";
	
	public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
	
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	private VideoFileManager videoDataMgr;
	
	VideoSvc() throws IOException {
		videoDataMgr = VideoFileManager.get();
	}

    public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
        videoDataMgr.saveVideoData(v, videoData.getInputStream());
    }
    
    public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
        // Of course, you would need to send some headers, etc. to the
        // client too!
        //  ...
        videoDataMgr.copyVideoData(v, response.getOutputStream());
   }
    
	    private Map<Long,Video> videos = new HashMap<Long, Video>();
//	    private Map<Long, MultipartFile> videosDB= new HashMap<Long, MultipartFile>();

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
	public ResponseEntity<VideoStatus> setVideoData(@PathVariable("id") long id, @RequestParam("data") MultipartFile videoData, HttpServletResponse response) throws
	ServletException, IOException{
		System.out.println("id received is " + id);
		// TODO Auto-generated method stub
		VideoStatus status = new VideoStatus(VideoState.READY);
		Video v = videos.get(id);
		//v.setContentType(videoData);
		if (v != null) {
			//videosDB.put(id, videoData);
			
			//response.setStatus(200);
			//response.setContentType("application/json");
			//response.send;
			
			//return status;
			//response.getWriter().
			System.out.println("saving video data");
			saveSomeVideo(v, videoData);
			return new ResponseEntity<VideoStatus>(status, HttpStatus.OK);
		} else {
			System.out.println("unable to save video data");
			return new ResponseEntity<VideoStatus>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.GET)
	@ResponseBody
    public void getVideoData(@PathVariable("id") long id, HttpServletResponse response) throws ServletException, IOException{
		System.out.println("id received is " + id);
		
		Video v = videos.get(id);
		if (v != null) {
			if (videoDataMgr.hasVideoData(v)) {
				System.out.println("Returning video");
				response.setStatus(200);
				serveSomeVideo(v, response);
				//return new ResponseEntity<MultipartFile>(HttpStatus.OK);
			}
			else {
				System.out.println("videosDB.get(id) is NULL");
				response.sendError(404);
				//return new ResponseEntity<MultipartFile>(HttpStatus.NOT_FOUND);
			}
		}
		else {
			response.sendError(404);
		}
		
		System.out.println("Returning NULL");
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

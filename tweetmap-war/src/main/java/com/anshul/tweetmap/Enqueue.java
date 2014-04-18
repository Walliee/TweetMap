package com.anshul.tweetmap;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

public class Enqueue extends HttpServlet {
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int file = Integer.parseInt(request.getParameter("file"));

        // Add the task to the default queue.
        Queue queue = QueueFactory.getDefaultQueue();
        for(int i=file; i<file+53; i++) {
        	queue.add(withUrl("/tweetmap").param("file", Integer.toString(i)));
        	queue.add(withUrl("/indexbuild").param("file", Integer.toString(i)));
        }

        response.sendRedirect("/");
    }
}
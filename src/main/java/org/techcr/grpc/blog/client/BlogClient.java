package org.techcr.grpc.blog.client;

import org.proto.blog.Blog;
import org.proto.blog.BlogReadRequest;
import org.proto.blog.BlogRequest;
import org.proto.blog.BlogResponse;
import org.proto.blog.BlogServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class BlogClient {

    public static void main(String[] args) {
        BlogClient blogClient = new BlogClient();
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50023)
            .usePlaintext()
            .build();

        blogClient.blogCRUDManager(channel);

        channel.shutdown();
    }

    private void blogCRUDManager(ManagedChannel channel) {
        System.out.println("Start Creating new block");
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
            .setAuthorId("Chamly")
            .setTitle("My First Blog")
            .setContent("My First Blog, Here i have learn how to write blog")
            .build();

        BlogResponse response = blogClient.createBlog(
            BlogRequest.newBuilder()
                .setBlog(blog)
                .build()
        );
        System.out.println("Blog Created- ID: " + response.getBlog().getId());

        String validId = response.getBlog().getId();
        String invalidId = "xx";

        response = blogClient.readBlog(BlogReadRequest.newBuilder().setBlogId(validId).build());
        Blog validBlog = response.getBlog();
        System.out.println("Find valid blog: " + validBlog.toString());

        try {
            response = blogClient.readBlog(BlogReadRequest.newBuilder().setBlogId(invalidId).build());
        } catch (StatusRuntimeException e) {
            System.out.println("Excetion on invalid blog id find " + e.getStatus().toString());
        }

        Blog updateBlog = validBlog.toBuilder()
            .setContent("Adding new content to BLOG")
            .setId(validId)
            .build();
        BlogResponse updatedResponse = blogClient.updateBlog(BlogRequest.newBuilder().setBlog(updateBlog).build());
        System.out.println("updated response: " + updatedResponse.getBlog().toString());


//        BlogResponse deleteResponse = blogClient.deleteBlog(BlogDeleteRequest.newBuilder().setBlogId(validId).build());
//        System.out.println("Blog Deleted: " + deleteResponse.getBlog().toString());

    }




}

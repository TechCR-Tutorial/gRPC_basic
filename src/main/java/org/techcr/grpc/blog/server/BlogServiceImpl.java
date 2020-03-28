package org.techcr.grpc.blog.server;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.proto.blog.Blog;
import org.proto.blog.BlogDeleteRequest;
import org.proto.blog.BlogReadRequest;
import org.proto.blog.BlogRequest;
import org.proto.blog.BlogResponse;
import org.proto.blog.BlogServiceGrpc;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received create blog request");
        Blog blog = request.getBlog();

        Document document = new Document("author_id", blog.getAuthorId())
            .append("title", blog.getTitle())
            .append("content", blog.getContent());
        collection.insertOne(document);

        String id = document.getObjectId("_id").toString();
        System.out.println("Blog created id: " + id);
        Blog responseBlog = blog.toBuilder().setId(id).build();

        responseObserver.onNext(BlogResponse.newBuilder()
            .setBlog(responseBlog)
            .build());
        responseObserver.onCompleted();
        System.out.println("Blog Create completed. ");

    }

    @Override
    public void readBlog(BlogReadRequest request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received Read blog request");
        String id =  request.getBlogId();
        Document document = null;
        try {
            document = collection.find(eq("_id", new ObjectId(id))).first();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        if (document == null) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("No Blog found for id: " + id)
                    .asRuntimeException());
        } else {
            Blog blog = Blog.newBuilder()
                .setId(id)
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content")).build();
            responseObserver.onNext(BlogResponse.newBuilder()
                .setBlog(blog)
                .build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void updateBlog(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received update blog request");
        Blog blog = request.getBlog();
        Document document = new Document("author_id", blog.getAuthorId())
            .append("title", blog.getTitle())
            .append("content", blog.getContent());
        collection.replaceOne(eq("_id", new ObjectId(blog.getId())), document);

        Document findByIdDoc = collection.find(eq("_id", new ObjectId(blog.getId()))).first();
        Blog responseBlog = Blog.newBuilder()
            .setId(blog.getId())
            .setAuthorId(findByIdDoc.getString("author_id"))
            .setTitle(findByIdDoc.getString("title"))
            .setContent(findByIdDoc.getString("content")).build();
        responseObserver.onNext(BlogResponse.newBuilder().setBlog(responseBlog).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogDeleteRequest request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received delete blog request");
        String id = request.getBlogId();
        Document findByIdDoc = collection.findOneAndDelete(eq("_id", new ObjectId(id)));
        Blog responseBlog = Blog.newBuilder()
            .setId(id)
            .setAuthorId(findByIdDoc.getString("author_id"))
            .setTitle(findByIdDoc.getString("title"))
            .setContent(findByIdDoc.getString("content")).build();
        responseObserver.onNext(BlogResponse.newBuilder().setBlog(responseBlog).build());
        responseObserver.onCompleted();
    }
}

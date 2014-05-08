package edu.sjsu.cmpe.library.api.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;
import org.zeromq.ZMQ;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.config.ConstantUtil;
import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.CommandWrapper;
import edu.sjsu.cmpe.library.domain.Review;
import edu.sjsu.cmpe.library.dto.AuthorDto;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.ReviewDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Message
public class BookResource {
    /** bookRepository instance not used anymore */
    //private final BookRepositoryInterface bookRepository;
    ZMQ.Context context = null;
	ZMQ.Socket socket = null;

	public void connect(String address) {
		this.context = ZMQ.context(1);
		this.socket = context.socket(ZMQ.REQ);
		// Socket to talk to server
		this.socket.connect(address);
	}

    /**
     * BookResource constructor
     * 
     * @param bookRepository
     *            a BookRepository instance
     */
    public BookResource(BookRepositoryInterface bookRepository) {
	//this.bookRepository = bookRepository;
	
	this.initializeHashMap();
    }
    
    public void initializeHashMap(){
    	//connect to server
    	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
    	
    	//send to server and initialize HashMap
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	final MessagePack msgpack = new MessagePack();
    	Packer packer = msgpack.createPacker(out);
    	byte raw[] = null;
		try {
			raw = msgpack.write(new ConcurrentHashMap<Long, Book>());
			Value mapVal = msgpack.read(raw);
			Value values[] = new Value[1];
			values[0] = mapVal;
			
			//Book bookChck = msgpack.read(raw, Book.class);
			//System.out.println("in book resource--- inside intialize map");
			
			packer.write(new CommandWrapper("createLibraryHashMap", values));
	        this.socket.send(out.toByteArray(), 0);
	        //System.out.println("End of method: SendToServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @GET
    @Path("/{isbn}")
    @Timed(name = "view-book")
    public Response viewBookbyISBN(@PathParam("isbn") LongParam isbn, 
    		@Context HttpHeaders headers){
    	BookDto bookResponse = getBookByIsbn(isbn);
    	Book book = bookResponse.getBook();
    	System.out.println("Book retrieved by getBookByIsbn--->"+book.getTitle());
    	
    	if(headers.getRequestHeader(headers.IF_MODIFIED_SINCE)!=null){
    		Date newDate = new Date(headers.getRequestHeader(headers.IF_MODIFIED_SINCE).get(0).toString());
    		ConcurrentHashMap<Long, Date> dateInMemoryMap = null; //bookRepository.getDateInMemoryMap();
    		//connect to server
        	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
        	
        	//send to server and initialize HashMap
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
        	final MessagePack msgpack = new MessagePack();
        	Packer packer = msgpack.createPacker(out);
    		try {
    			packer.write(new CommandWrapper("getDateInMemoryMap", null));
    	        this.socket.send(out.toByteArray(), 0);
    	        
    	        //listen ton server to get the Map
    	        dateInMemoryMap = getMapFromServer();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		if(dateInMemoryMap.contains(isbn.get())){
    			Date anotherDate = dateInMemoryMap.get(isbn.get());
    			if(newDate.compareTo(anotherDate)==0){
    				return Response.status(304).entity("Not Modified").build();
    			}
    		}else{
    			try {
        			byte[] rawIsbn = msgpack.write(isbn.get());
        			byte[] rawDate = msgpack.write(newDate);

        			Value values[] = new Value[2];
        			values[0] = msgpack.read(rawIsbn);
        			values[1] = msgpack.read(rawDate);
        			
        			//bookRepository.setDateInMemoryMap(isbn.get(), newDate);
        			packer.write(new CommandWrapper("setDateInMemoryMap", values));
        	        this.socket.send(out.toByteArray(), 0);
        	        
        		}catch(Exception e){
        			e.printStackTrace();
        		}
    		}
    	}
    	
    	AuthorDto authorLinks = new AuthorDto();
		if(book.getAuthorList()!=null && !book.getAuthorList().isEmpty()){
			for(Author authorObj : book.getAuthorList()){
				authorLinks.addLink(new LinkDto("view-author", "/books/" + 
						book.getIsbn() + "/authors/" + authorObj.getId(), "GET"));
			}
		}
		
		ReviewDto reviewLinks = new ReviewDto();
		if(book.getReviewList()!=null && book.getReviewList().size() > 0){
			for(Review reviewObj : book.getReviewList()){
				reviewLinks.addLink(new LinkDto("view-review", "/books/" + book.getIsbn() + 
						"/reviews/" + reviewObj.getId(), "GET"));
			}
		}
		
		Map<String, Object> bookMap = new HashMap<String, Object>();
		bookMap.put("isbn", book.getIsbn());
		bookMap.put("title", book.getTitle());
		bookMap.put("publication-date", book.getPublication_date());
		bookMap.put("language", book.getLanguage());
		bookMap.put("num-pages", book.getNum_pages());
		bookMap.put("status", book.getStatus());
		bookMap.put("reviews", reviewLinks.getLinks());
		bookMap.put("authors", authorLinks.getLinks());
		
		
		Map<String, Object> displayMap = new HashMap<String, Object>();
		displayMap.put("book", bookMap);
		displayMap.put("links", bookResponse.getLinks());
		
		return Response.status(200).entity(displayMap).build();
    }
    
    
}

package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Custom Iterable class for iterating through the file contents
 * @author Deepen
 *
 */
public class BufferedReaderIterable implements Iterable<String> {

    private BufferedReader br;
    private Iterator<String> i;
    
    /**
     * Constructor for BufferedReaderIterable class that accepts a BufferedReader object
     * @param br
     */
    public BufferedReaderIterable( BufferedReader br ) {
        i = new BufferedReaderIterator( br );
    }
    
    /**
     * Constructor for BufferedReaderIterable class that accepts a File object
     * @param f
     */
    public BufferedReaderIterable( File f ) throws FileNotFoundException {
        br = new BufferedReader( new FileReader( f ) );
        i = new BufferedReaderIterator( br );
    }
    
    /**
     * Constructor for Iterator
     */
    public Iterator<String> iterator() {
        return i;
    }
    
    /**
     * Custom BufferedReaderIterator class
     * @author Deepen
     *
     */
    private class BufferedReaderIterator implements Iterator<String> {
        private BufferedReader br;
        private String line;
        
        /**
         * Constructor for BufferedReaderIterator that accepts a BufferedIterator object
         * @param aBR
         */
        public BufferedReaderIterator( BufferedReader aBR ) {
            (br = aBR).getClass();
            advance();
        }
        
        /**
         * Method to check whether the end of file has reached/
         */
        public boolean hasNext() {
            return line != null;
        }
        
        /**
         * Generic method next()
         */
        public String next() {
            String retval = line;
            advance();
            return retval;
        }
        
        /**
         * Generic method remove
         */
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported on BufferedReader iteration.");
        }
        
        /**
         * Method to close the iterator if it has finished reading the file
         */
        private void advance() {
            try {
                line = br.readLine();
            }
            catch (IOException e) { /* TODO */}
            if ( line == null && br != null ) {
                try {
                    br.close();
                }
                catch (IOException e) { /* Ignore - probably should log an error */ }
                br = null;
            }
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException {
    	File file = new File("README.md");
    	BufferedReaderIterable bufferedReaderIterable = new BufferedReaderIterable(file);    	
    	for (String s : bufferedReaderIterable) {
    		System.out.println(s);
    	}
	}
}

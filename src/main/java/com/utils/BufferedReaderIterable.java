package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class BufferedReaderIterable implements Iterable<String> {

    private BufferedReader br;
    private Iterator<String> i;

    public BufferedReaderIterable( BufferedReader br ) {
        i = new BufferedReaderIterator( br );
    }

    public BufferedReaderIterable( File f ) throws FileNotFoundException {
        br = new BufferedReader( new FileReader( f ) );
        i = new BufferedReaderIterator( br );
    }
    public Iterator<String> iterator() {
        return i;
    }

    private class BufferedReaderIterator implements Iterator<String> {
        private BufferedReader br;
        private String line;

        public BufferedReaderIterator( BufferedReader aBR ) {
            (br = aBR).getClass();
            advance();
        }

        public boolean hasNext() {
            return line != null;
        }

        public String next() {
            String retval = line;
            advance();
            return retval;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported on BufferedReader iteration.");
        }

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

package org.kbsriram.android.prw.data;

// This code will pull out a single line from a text file that contains
// a bunch of lines.
//
// The first line in the text file is expected to be a number, with the
// total number of lines in the file.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CSimpleLineDatabase
{

    // given an index and a stream, this will return the (index mod
    // #lines) line within the file.  eg: given a file with 5 lines
    // and an index 12, this will return the 3rd line in the
    // file. index 10 in the same file will give the first line in the
    // file.
    public final static String getLine(long index, InputStream in)
        throws IOException
    {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in), 8192);
            long max = Long.parseLong(br.readLine());

            long actual = index % max;
            long cur = 0l;
            while (cur < actual) {
                br.readLine(); // skip past this many.
                cur++;
            }
            return br.readLine();
        }
        finally {
            if (br != null) {
                try { br.close(); }
                catch (IOException ign) {}
            }
        }
    }
}
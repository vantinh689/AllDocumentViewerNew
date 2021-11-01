
package com.reader.office.officereader.search;

import java.io.File;

public interface ISearchResult
{
    /**
     * return to Search Results. call with has match file
     * 
     * @param 
     */
    public void onResult(File file);
    
    /**
     * call with search finish
     */
    public void searchFinish();
    
}

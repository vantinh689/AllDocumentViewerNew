package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities;

import android.content.Context;
import android.view.MotionEvent;

import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class PDFViewScrollHandler extends DefaultScrollHandle {


    private  onScrollerEvents listener;
    private int pageNum;

    public PDFViewScrollHandler(Context context) {
        super(context);
    }

    public PDFViewScrollHandler(Context context, boolean inverted) {
        super(context, inverted);
    }

    public PDFViewScrollHandler(Context context, onScrollerEvents listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public void setPageNum(int pageNum) {
        super.setPageNum(pageNum);
        this.pageNum = pageNum;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(listener != null) {
                    listener.onShowScroller(pageNum);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if(listener != null) {
                    listener.onHideScroller();
                }
                break;
        }
        return super.onTouchEvent(event);
    }



    public interface onScrollerEvents {
        void onShowScroller(int pagesScrolled);
        void onHideScroller();
    }
}

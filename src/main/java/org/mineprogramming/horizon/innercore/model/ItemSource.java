package org.mineprogramming.horizon.innercore.model;

import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class ItemSource {

    static {
    }

    // To notify listener even if not set yet
    private boolean loadedLastNotified = true;

    public interface ModSourceListener {
        public void onChange();
        public void onLoadLast();
        public void onLoadFailed();
        public void onLoadInProgress();
    };


    private List<Item> items = new ArrayList<>();
    private ModSourceListener modSourceListener;


    public Item get(int index){
        return items.get(index);
    }


    public int getItemCount(){
        return items.size();
    }


    public void setOnChangeListener(ModSourceListener listener){
        this.modSourceListener = listener;

        if(!loadedLastNotified){
            loadedLastNotified = true;
            listener.onLoadLast();
        }
    }


    protected void addItem(Item mod){
        items.add(mod);
        notifyChange();
    }

    protected void clearItems(){
        items.clear();
        notifyChange();
    }


    private void notifyChange(){
        if(modSourceListener != null){
            modSourceListener.onChange();
        }
    }

    
    protected void notifyLoadLast(){
        if(modSourceListener != null){
            modSourceListener.onLoadLast();
        } else {
            loadedLastNotified = false;
        }
    }

    protected void notifyLoadFailed(){
        if(modSourceListener != null){
            modSourceListener.onLoadFailed();
        }
    }

    protected void notifyLoadInProgress(){
        if(modSourceListener != null){
            modSourceListener.onLoadInProgress();
        }
    }

    public void retryLoad(){
        // PLACEHOLDER
    }

    public void requestMore(){
        // PLACEHOLDER
    }

    public void updateList(){
        // PLACEHOLDER
    }


	public boolean contains(Item item) {
		return items.contains(item);
	}

}
package com.example.androidfilters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class ViewEventAdapter extends RecyclerView.Adapter<ViewEventAdapter.ViewHolder> {
    private List<ThumbnailItem> item;
    ViewEventAdapter.ThumbnailsAdapterListener listener1;
    Context c1;
    public ViewEventAdapter(Context context,List<ThumbnailItem> it,ViewEventAdapter.ThumbnailsAdapterListener listener){
        c1=context;
        listener1=listener;
        item=it;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.thumnail_list_item,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        final ThumbnailItem item1=item.get(position);
        viewHolder.title.setText(item1.filterName);
        viewHolder.img.setImageBitmap(item1.image);
        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener1.onFilterSelected(item1.filter);
//                selectedIndex = position;
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img=(ImageView) itemView.findViewById(R.id.img);
            title=(TextView)itemView.findViewById(R.id.filter_name);

        }
    }


    public interface ThumbnailsAdapterListener {
        void onFilterSelected(Filter filter);
    }
}

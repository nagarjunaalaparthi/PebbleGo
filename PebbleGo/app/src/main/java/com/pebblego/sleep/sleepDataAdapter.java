package com.pebblego.sleep;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pebblego.R;
import com.pebblego.SharedPreferenceUtils;

/**
 * Created by Arjun.
 */
public class sleepDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    public sleepDataAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_list_item, parent, false);
        return new DataViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DataViewHolder viewHolder = (DataViewHolder) holder;
        viewHolder.goal.setText("goal : " + SharedPreferenceUtils.readString(context, "count", ""));
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {
        private TextView goal;
        private TextView current;
        private TextView remaining;

        public DataViewHolder(View itemView) {
            super(itemView);
            goal = (TextView) itemView.findViewById(R.id.goal_count);
            current = (TextView) itemView.findViewById(R.id.count);
            remaining = (TextView) itemView.findViewById(R.id.remaining_count);
        }

    }
}

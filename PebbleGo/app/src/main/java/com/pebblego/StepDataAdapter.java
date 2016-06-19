package com.pebblego;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Arjun.
 */
public class StepDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    public StepDataAdapter(Context context) {
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
        viewHolder.goal.setText(context.getString(R.string.goal,SharedPreferenceUtils.readString(context, "count", "")));
        int goalCount = Integer.parseInt(SharedPreferenceUtils.readString(context, "count", "0"));
        int stepCount = SharedPreferenceUtils.readInteger(context,"step_count",0);
        int remaining = goalCount - stepCount;
        viewHolder.current.setText(context.getString(R.string.steps,stepCount));
        if(remaining > 0){
            viewHolder.remaining.setText(context.getString(R.string.remaining_count,remaining));
        }else{
            viewHolder.remaining.setText("Hurray!!! Reached the Goal for today");
        }
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

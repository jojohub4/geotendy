package com.example.geotendy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LecturerUnitsAdapter extends RecyclerView.Adapter<LecturerUnitsAdapter.ViewHolder> {
    private List<CourseUnit> units;
    private Context context;

    public LecturerUnitsAdapter(List<CourseUnit> units, Context context) {
        this.units = (units != null) ? units : new ArrayList<>(); // ✅ Prevent null pointer
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_unit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseUnit unit = units.get(position);
        holder.unitName.setText(unit.getUnitName());

        holder.itemView.setOnClickListener(v -> {
            Log.d("LecturerUnitsAdapter", "Unit clicked: " + unit.getUnitName());
        });

    }



    @Override
    public int getItemCount() {
        return (units != null) ? units.size() : 0;
    }

    public void updateData(List<CourseUnit> newUnits) {
        this.units = newUnits;
        notifyDataSetChanged();  // ✅ Refresh UI when data changes
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView unitName;

        public ViewHolder(View itemView) {
            super(itemView);
            unitName = itemView.findViewById(R.id.unitName);
        }
    }
}


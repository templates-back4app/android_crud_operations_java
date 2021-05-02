package com.demarkelabs.crud_guide_java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseObject;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoHolder> {

    private List<ParseObject> list;
    private Context context;
    public MutableLiveData<ParseObject> onEditListener = new MutableLiveData<>();
    public MutableLiveData<ParseObject> onDeleteListener = new MutableLiveData<>();

    public TodoAdapter(List<ParseObject> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public TodoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item,parent,false);
        return new TodoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoHolder holder, int position) {
        ParseObject object = list.get(position);
        holder.title.setText(object.getString("title"));
        holder.description.setText(object.getString("description"));

        holder.edit.setOnClickListener(v -> {
            onEditListener.postValue(object);
        });

        holder.delete.setOnClickListener(v -> {
            onDeleteListener.postValue(object);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class TodoHolder extends RecyclerView.ViewHolder{
    TextView title;
    TextView description;
    ImageView edit;
    ImageView delete;


    public TodoHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
        edit = itemView.findViewById(R.id.edit);
        delete = itemView.findViewById(R.id.delete);
    }
}

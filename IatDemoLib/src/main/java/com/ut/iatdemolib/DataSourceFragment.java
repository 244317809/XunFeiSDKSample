package com.ut.iatdemolib;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DataSourceFragment extends Fragment implements SpeechSupportable {


    private static final int TICKETS_SIZE = 50;
    private Spinner mSpinner;
    private ImageButton mStartButton;
    private TextView mTextView;

    private List<Ticket> mTotalTickets;
    private List<Ticket> mFilterTickets;
    private RecyclerView mRecyclerView;
    private DataSourceAdapter mDataSourceAdapter;

    private IatActivity mHost;
    private List<String> mNames;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHost = (IatActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_source, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartButton = view.findViewById(R.id.btn_start);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHost.startIatRecognize();
            }
        });
        mStartButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mHost.startIatRecognize();
                return true;
            }
        });

        mNames = Arrays.asList(getContext().getResources().getStringArray(R.array.names));

        mSpinner = view.findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFilterTickets = new ArrayList<>(mTotalTickets);// Refresh data
                if (position != 0) { // position=0 --> ALL
                    String name = mNames.get(position);
                    for (Ticket ticket : mTotalTickets) {
                        if (!name.equals(ticket.creator)) {
                            mFilterTickets.remove(ticket);
                        }
                    }
                }

                mDataSourceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTotalTickets = new ArrayList<>();
        Random random = new Random();
        List<String> data = mNames.subList(1, mNames.size());
        for (int i = 0; i < TICKETS_SIZE; i++) {
            int r = random.nextInt(100);
            String creator = data.get((i + r)%data.size());
            String title = "待办工作票#" + i;
            Ticket ticket = new Ticket(title, creator);
            mTotalTickets.add(ticket);
        }
        mFilterTickets = new ArrayList<>(mTotalTickets); // Copy when init

        mDataSourceAdapter = new DataSourceAdapter();
        mRecyclerView.setAdapter(mDataSourceAdapter);

        // Txt
        mTextView = view.findViewById(R.id.txt);
        StringBuilder stringBuilder = new StringBuilder("你可以说：\n\n");
        for (String item : mNames) {
            stringBuilder.append(item).append("\n");
        }
        mTextView.setText(stringBuilder.toString());
    }

    @Override
    public void onSpeechRecognizeResult(String result) {
        int index = mNames.indexOf(result);
        if (index != -1) {
            mSpinner.setSelection(index);
        } else {
            mHost.showTip("未能在数据源中识别成功");
        }
    }


    private class DataSourceAdapter extends RecyclerView.Adapter<DataSourceAdapter.ViewHolder> {


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.item_search, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.title.setText(mFilterTickets.get(position).title);
            holder.creator.setText(mFilterTickets.get(position).creator);
        }

        @Override
        public int getItemCount() {
            return mFilterTickets.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView title;
            private TextView creator;

            public ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tv_title);
                creator = itemView.findViewById(R.id.tv_creator);
            }
        }
    }

    private class Ticket {
        private String title;
        private String creator;

        public Ticket(String title, String creator) {
            this.title = title;
            this.creator = creator;
        }
    }
}

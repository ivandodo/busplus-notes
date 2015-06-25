package com.ivandodo.busplusnotes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivandodo.busplusnotes.R;
import com.ivandodo.busplusnotes.entity.BusPlusCard;

/**
* Created by ivan.radojevic on 25.06.2015..
*/

public class BusplusCardViewAdapter extends AbstractListAdapter<BusPlusCard, BusplusCardViewAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private       OnItemClickListener mOnItemClickListener;

    public BusplusCardViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(
                mInflater.inflate(R.layout.bus_plus_kartica, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bind(mData.get(i));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView cardnameTextView;
        private final TextView cardSerialTextView;
        private final TextView cardAmmountTextView;
        private final ImageButton cardAction1ImageButton;
        private final ImageButton cardAction2ImageButton;

        private BusPlusCard mEntity;

        public ViewHolder(View v) {
            super(v);

            cardnameTextView = (TextView) v.findViewById(R.id.cardName);
            cardSerialTextView = (TextView) v.findViewById(R.id.cardSerial);
            cardAmmountTextView = (TextView) v.findViewById(R.id.cardAmmount);
            cardAction1ImageButton = (ImageButton) v.findViewById(R.id.action1Button);
            cardAction2ImageButton = (ImageButton) v.findViewById(R.id.action2Button);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mEntity);
                    }
                }
            };

            v.setOnClickListener(listener);
        }

        public void bind(BusPlusCard entity) {
            mEntity = entity;
            cardnameTextView.setText(entity.getName());
            cardSerialTextView.setText(entity.getSerial());
            cardAmmountTextView.setText(entity.getAmmount().toString());
        }

        @Override
        public String toString() {
            return "ViewHolder{" + cardSerialTextView.getText() + "}";
        }
    }

    public static interface OnItemClickListener {
        public void onItemClick(BusPlusCard entity);
    }
}
package dekauliya.fyp.mathqa.UI.TopicList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.List;

import dekauliya.fyp.mathqa.Models.KeyPoint;
import dekauliya.fyp.mathqa.R;
import dekauliya.fyp.mathqa.Utils.GraphicUtils;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Created by dekauliya on 2/2/17.
 */

public class KeyPointHeaderItem extends AbstractExpandableHeaderItem<KeyPointHeaderItem
        .KeypointHeaderViewHolder, KeyPointSubItem> {

    private KeyPoint keypoint;

    public KeyPointHeaderItem(KeyPoint keypoint) {
        this.keypoint = keypoint;
    }

    public KeyPoint getKeyPoint() {
        return keypoint;
    }

    public void setKeypoint(KeyPoint keypoint) {
        this.keypoint = keypoint;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof  KeyPointHeaderItem){
            KeyPointHeaderItem k = (KeyPointHeaderItem) o;
            return this.getKeyPoint().equals(k.getKeyPoint());
        }
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_text_header_item;
    }

    @Override
    public KeypointHeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new KeypointHeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false),
                adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, KeypointHeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Logger.d("ExpandableHeaderItem Payload " + payloads);
        } else {
            holder.mKeyPointTitle.setText(keypoint.getName().replaceAll("_", " "));
        }
    }

    static class KeypointHeaderViewHolder extends ExpandableViewHolder{
        TextView mKeyPointTitle;

        KeypointHeaderViewHolder(View view, FlexibleAdapter adapter){
            super(view, adapter, true);
            mKeyPointTitle = (TextView) view.findViewById(R.id.rv_header_title_text);
        }

        @Override
        public float getActivationElevation() {
            return GraphicUtils.dpToPx(itemView.getContext(), 4f);
        }

        @Override
        protected boolean shouldNotifyParentOnClick() {
            return true;
        }
    }

    @Override
    public String toString() {
        return "KeyPoint[" + keypoint.getName() + "//SubItems" + super.getSubItems() + "]";
    }
}
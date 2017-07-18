/*
 * Copyright (c) 2017. Ruslan Primak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 7/15/17 1:27 AM
 */

package link.primak.booklistingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import link.primak.booklistingapp.databinding.VolumeListItemBinding;


public class VolumesAdapter extends ArrayAdapter<VolumeInfo> {

    public VolumesAdapter(@NonNull Context context) {
        super(context, 0);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        VolumeListItemBinding itemBinding;
        if (convertView == null) {
            itemBinding = VolumeListItemBinding.inflate(LayoutInflater.from(getContext()), parent, false);
            convertView = itemBinding.getRoot();
            convertView.setTag(itemBinding);
        } else {
            itemBinding = (VolumeListItemBinding) convertView.getTag();
        }

        VolumeInfo item = getItem(position);
        if (item != null) {
            itemBinding.thumbnail.setImageBitmap(item.getImage());
            itemBinding.textAuthor.setText(item.getAuthor());
            itemBinding.textTitle.setText(item.getTitle());
        } else {
            itemBinding.textAuthor.setText("null");
            itemBinding.textTitle.setText("null");
        }

        return convertView;
    }
}

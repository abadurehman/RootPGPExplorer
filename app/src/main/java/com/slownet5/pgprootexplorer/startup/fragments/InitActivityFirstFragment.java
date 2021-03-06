/*
 * Copyright (c) 2017. slownet5
 *  This file is part of RootPGPExplorer also known as CryptoFM
 *
 *       RootPGPExplorer a is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       RootPGPExplorer is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with RootPGPExplorer.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.slownet5.pgprootexplorer.startup.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slownet5.pgprootexplorer.R;
import com.slownet5.pgprootexplorer.startup.InitActivity;

/**
 * Created by tripleheader on 11/28/16.
 */

public class InitActivityFirstFragment extends Fragment {
    InitFragmentCallbacks callbacks;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.first_fragment,null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callbacks=(InitActivity)getActivity();
        callbacks.viewCreated();
    }
}

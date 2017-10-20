/*
 * Copyright (C) 2017 nekocode (nekocode.cn@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.nekocode.hot.ui.setting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.evernote.android.state.State;
import com.evernote.android.state.StateSaver;

import java.util.ArrayList;

import cn.nekocode.hot.HotApplication;
import cn.nekocode.hot.R;
import cn.nekocode.hot.base.BaseActivity;
import cn.nekocode.hot.data.model.Column;
import cn.nekocode.hot.data.model.ColumnPreference;
import cn.nekocode.hot.databinding.ActivityColumnMangerBinding;
import cn.nekocode.hot.manager.base.BaseColumnManager;
import cn.nekocode.hot.manager.base.BasePreferenceManager;
import cn.nekocode.hot.util.DividerItemDecoration;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ColumnManagerActivity extends BaseActivity implements ColumnListAdapter.UIEventListener {
    private ActivityColumnMangerBinding mBinding;
    @State
    public ArrayList<ColumnPreference> mPreferences;
    private BaseColumnManager mColumnManager;
    private BasePreferenceManager mPreferenceManager;
    private ColumnListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateSaver.restoreInstanceState(this, savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_column_manger);
        mColumnManager = HotApplication.getDefaultColumnManager(this);
        mPreferenceManager = HotApplication.getDefaultPreferenceManager(this);

        /*
          Data initialize
         */
        if (mPreferences == null) {
            mPreferences = new ArrayList<>();
            loadColumnPreferences();
        }


        /*
          View initialize
         */
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Setup the recyclerview
        mAdapter = new ColumnListAdapter(mPreferences);
        mAdapter.setUIEventListener(this);

        mBinding.recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setItemAnimator(null);
        mBinding.recyclerView.addItemDecoration(DividerItemDecoration.obtainDefault(this));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        StateSaver.saveInstanceState(this, outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemsSwapped() {
        mPreferenceManager.saveColumnPreferences(mPreferences);
    }

    @Override
    public void onItemVisibilityButtonClick(int position, ColumnPreference preference) {
        mPreferenceManager.updateColumnPreferences(preference);
    }

    @Override
    public void onItemUninstallButtonClick(int position, ColumnPreference preference) {
        showUninstallDialog(preference.getColumn(), () -> {
            // When uninstall success
            mPreferences.remove(position);
            mPreferenceManager.removeColumnPreferences(preference);
            mAdapter.notifyItemRemoved(position);
        });
    }

    private void loadColumnPreferences() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        mColumnManager.getAllInstalled()
                .subscribeOn(Schedulers.io())
                .compose(bindToLifecycle())
                .flatMap(columns -> mPreferenceManager.loadColumnPreferences(columns))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(preferences -> {
                    progressDialog.dismiss();
                    mPreferences.clear();
                    mPreferences.addAll(preferences);
                    mAdapter.notifyDataSetChanged();

                }, throwable -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, R.string.toast_load_columns_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void showUninstallDialog(Column column, final Runnable successCallback) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_ensure_uninstall_column, column.getName(), column.getVersion()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    uninstallColumn(column, successCallback);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void uninstallColumn(Column column, final Runnable successCallback) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.dialog_uninstalling_column));
        progressDialog.setCancelable(false);
        progressDialog.show();

        mColumnManager.uninstall(column.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(column2 -> {
                    successCallback.run();
                    progressDialog.dismiss();
                    Toast.makeText(ColumnManagerActivity.this,
                            R.string.toast_uninstall_column_success, Toast.LENGTH_SHORT).show();

                }, throwable -> {
                    progressDialog.dismiss();
                    Toast.makeText(ColumnManagerActivity.this,
                            R.string.toast_uninstall_column_failed, Toast.LENGTH_SHORT).show();
                });
    }
}
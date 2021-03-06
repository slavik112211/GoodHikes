/*------------------------------------------------------------------------------
 *   Authors: Slavik, George, Thao, Chelsea
 *   Copyright: (c) 2016 Team Magic
 *
 *   This file is part of GoodHikes.
 *
 *   GoodHikes is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GoodHikes is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GoodHikes.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uwaterloo.magic.goodhikes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.Route;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesDatabaseManager;
import ca.uwaterloo.magic.goodhikes.data.UserManager;

public class HistoryActivity extends AppCompatActivity {
    private UserManager userManager;
    private RoutesDatabaseManager database;
    private ArrayList<Route> routes;
    private RoutesAdapter routesAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    public static final String DELETE_ROUTE = "deleteRoute";
    public static final String POSITION_ID = "positionId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        database = RoutesDatabaseManager.getInstance(this);
        userManager = new UserManager(getApplicationContext());
        routes = database.getAllRoutes(Route.filterByUser(userManager.getUser()));
        routesAdapter = new RoutesAdapter(this, routes);
        View rootView = findViewById(android.R.id.content);

        mListView = (ListView) rootView.findViewById(R.id.routes_list);
        mListView.setAdapter(routesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Route route = (Route) adapterView.getItemAtPosition(position);
                if (route != null) {
                    Intent intent = new Intent();
                    intent.putExtra(RouteEntry._ID, route.getId());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                mPosition = position;
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(deleteItemMessageReceiver,
                new IntentFilter(DELETE_ROUTE));
    }

    private BroadcastReceiver deleteItemMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra(POSITION_ID, -1);
            Route route = routesAdapter.getItem(position);
            int result = database.deleteRoute(route.getId());
            if(result==1){
                routesAdapter.remove(route);
                routesAdapter.notifyDataSetChanged();
                Toast.makeText(context, "Route removed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteItemMessageReceiver);
        super.onDestroy();
    }


}

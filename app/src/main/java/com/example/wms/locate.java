package com.example.wms;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class locate extends Fragment {
    private int cells = 20;
    private Node[][] map;
    private MapView mapView;
    private int startx = -1;
    private int starty = -1;
    private int finishx = -1;
    private int finishy = -1;
    private String[] itemLocations; // Define item locations here
    private String list;
    private ApiService apiService;
    private ListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locate, container, false);

        LinearLayout canvasLayout = view.findViewById(R.id.canvas_layout);

        if (getArguments() != null) {
            list = getArguments().getString("listID", "err");

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://wms-api-u98x.onrender.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            // Create an instance of the ApiService interface
            apiService = retrofit.create(ApiService.class);

            Call<List<Item>> call = apiService.getItemByListID(list);

            call.enqueue(new Callback<List<Item>>() {
                @Override
                public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                    if (response.isSuccessful()) {
                        List<Item> listItems = response.body();

                        if(!listItems.isEmpty()) {
                            itemLocations = new String[listItems.size()]; // Initialize the array with the size of listItems

                            // Iterate through the list of items
                            for (int i = 0; i < listItems.size(); i++) {
                                // Assuming each Item has a getLocation() method returning String location
                                itemLocations[i] = listItems.get(i).getLocation();
                            }

                            mapView = new MapView(requireContext(), null); // Pass null AttributeSet
                            canvasLayout.addView(mapView);

                            generateMap();
                            findPath();
                        }
                        else {
                            Toast.makeText(requireContext(), "List Not Found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle error response
                        Toast.makeText(requireContext(), "List Not Found", Toast.LENGTH_SHORT).show();
                    }
                }


                @Override
                public void onFailure(Call<List<Item>> call, Throwable t) {
                    // Handle failure
                }
            });
        }

        return view;
    }

    private void generateMap() {
        map = new Node[cells + 17][cells];
        for (int x = 0; x < cells + 17; x++) {
            for (int y = 0; y < cells; y++) {
                map[x][y] = new Node(x, y);
            }
        }

        // Set walls and start/finish positions
        for (int x = 4; x < cells+17; x++) {
            map[x][0].setType(Node.WALL);  // Top border
        }
        for (int x = 0; x < cells+17; x++) {
            map[x][cells - 1].setType(Node.WALL); // Bottom border
        }

        for (int y = 0; y < cells - 1; y++) {
            map[0][y].setType(Node.WALL);  // Left border
        }
        for (int y = 1; y < cells - 1; y++) {
            map[cells+17 - 1][y].setType(Node.WALL); // Right border
        }

        // Set rack for item positions
        int[] rackPositions = {5, 6, 11, 12, 17, 18, 23, 24, 29, 30};
        int count = 1;
        for (int x : rackPositions) {
            setVerticalRack(x, count);
            count += 8;
        }

        startx = 1; // Starting position (adjust as needed)
        starty = 0;
        map[startx][starty].setType(Node.START);

        int cellSize = 50;
        mapView.setWarehouse(map, cellSize);
    }

    private void setVerticalRack(int x, int count) {
        for (int y = 4; y < cells - 12; y++) {
            map[x][y].setType(Node.WALL);
        }

        for (int y = 12; y < cells - 4; y++) {
            map[x][y].setType(Node.WALL);
        }

        for(String i : itemLocations){
            int cc = count;
            for (int y = 4; y < cells - 12; y++) {
                String prefix = getPrefix(i); // Get the appropriate prefix based on the item location
                String nodeName = prefix + String.format("%02d", cc);
                if (i.equals(nodeName)) {
                    finishx = x;
                    finishy = y;
                    map[finishx][finishy].setType(Node.FINISH);
                }
                cc++;
            }

            for (int y = 12; y < cells - 4; y++) {
                String prefix = getPrefix(i); // Get the appropriate prefix based on the item location
                String nodeName = prefix + String.format("%02d", cc);
                if (i.equals(nodeName)) {
                    finishx = x;
                    finishy = y;
                    map[finishx][finishy].setType(Node.FINISH);
                }
                cc++;
            }
        }
    }

    private String getPrefix(String itemLocation) {
        // Extract the prefix from the item location (e.g., "WC-01-", "WC-02-", etc.)
        return itemLocation.substring(0, 6); // Adjust the substring length based on your naming convention
    }

    private void findPath() {
        for (String itemLocation : itemLocations) {
            int finishX = -1;
            int finishY = -1;
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (map[x][y].getType() == Node.FINISH) {
                        finishX = x;
                        finishY = y;
                        break;
                    }
                }
                if (finishX != -1 && finishY != -1) {
                    break;
                }
            }

            if (finishX != -1 && finishY != -1) {
                map[startx][starty].setType(Node.START);

                PriorityQueue<Node> openSet = new PriorityQueue<>((n1, n2) -> Double.compare(n1.fScore, n2.fScore));
                Set<Node> closedSet = new TreeSet<>();

                map[startx][starty].gScore = 0;
                map[startx][starty].hScore = calculateHeuristic(map[startx][starty], map[finishX][finishY]);
                map[startx][starty].fScore = map[startx][starty].gScore + map[startx][starty].hScore;

                openSet.add(map[startx][starty]);
                map[startx][starty].setType(Node.OPENED);

                while (!openSet.isEmpty()) {
                    Node current = openSet.poll();
                    if (current.equals(map[finishX][finishY])) {
                        reconstructPath(current);
                        break; // Move to the next item location
                    }

                    closedSet.add(current);
                    current.setType(Node.CLOSED);

                    for (Node neighbor : getNeighbors(current)) {
                        if (closedSet.contains(neighbor) || neighbor.getType() == Node.WALL) {
                            continue;
                        }

                        double tentativeGScore = current.gScore + 1; // Assuming constant cost for moving between nodes

                        if (!openSet.contains(neighbor) || tentativeGScore < neighbor.gScore) {
                            neighbor.parent = current;
                            neighbor.gScore = tentativeGScore;
                            neighbor.hScore = calculateHeuristic(neighbor, map[finishX][finishY]);

                            neighbor.fScore = neighbor.gScore + neighbor.hScore;
                            neighbor.setType(Node.OPENED);

                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);

        // Update map to visualize the path (example)
        for (Node n : path) {
            map[n.getX()][n.getY()].setType(Node.FINISH); // Change type to represent path (adjust color in WarehouseMapView)
        }

        mapView.invalidate(); // Trigger redraw of the map

        return path;
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int x = node.getX();
        int y = node.getY();

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (newX >= 0 && newX < cells+17 && newY >= 0 && newY < cells) {
                neighbors.add(map[newX][newY]);
            }
        }

        return neighbors;
    }

    private double calculateHeuristic(Node node, Node goal) {
        // Manhattan distance heuristic (replace if needed)
        return Math.abs(node.getX() - goal.getX()) + Math.abs(node.getY() - goal.getY());
    }

    public class Node implements Comparable<Node> {
        public static final int EMPTY = 0;
        public static final int WALL = 1;
        public static final int START = 2;
        public static final int FINISH = 3;
        public static final int OPENED = 4;
        public static final int CLOSED = 5;

        private int x;
        private int y;
        private Node parent;
        private double gScore;
        private double hScore;
        private double fScore;
        private int type; // 0 = empty, 1 = wall, 2 = start, 3 = finish, 4 = opened, 5 = closed

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.type = EMPTY;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public int compareTo(Node other) {
            // Compare nodes based on their fScore
            return Double.compare(this.fScore, other.fScore);
        }
        // Getter and setter methods for x, y, parent, gScore, hScore, fScore, and type
    }
}

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Image;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.awt.image.BufferedImage;

public class github {
    static int num = 1;
    static int[][] grid;
    static int gridSize, cellSize;
    static int[] dx = {1, 0, -1, 0};
    static int[] dy = {0, 1, 0, -1};
    static boolean connected = false;
    static int[][] color;
    static boolean[][] visited;
    static String path;
    static PrintWriter out;
    static int picMagnifier = 10;
    static int directoryNum = 7;
    static int loop;
    static int[] map = {Color.WHITE.getRGB(), Color.BLUE.getRGB(), Color.CYAN.getRGB()};
    public static void main(String[] args) throws Exception {

        path = "../../Sheryl updates/images/color/"+directoryNum+"/";

        File file = new File(path);
        Files.createDirectories(file.toPath());

        out = new PrintWriter(new BufferedWriter(new FileWriter(path+"params.txt")));
        dumpData();
        
        gridSize = 50;
        cellSize = 15;
        int spawnX = 15;
        int spawnY = 25;
        grid = new int[gridSize][gridSize];
        color = new int[gridSize][gridSize];
        //2 is cytoskeleton/boundary, 1 is cytoplasm, 0 is outside

        //spawn two cells
        spawn(spawnX, spawnY, 1);
        spawnX = 35;
        spawn(spawnX, spawnY, 2);

        visited = new boolean[gridSize][gridSize];


        int numIterations = Integer.MAX_VALUE/3;
        out.println("numIterations "+numIterations);
        int maxNumMoves = 1000;
        out.println("n = "+maxNumMoves);
        int maxBubbleNeighbors = 3;
        out.println("s = "+maxBubbleNeighbors);
        int printFrequency = 50;
        out.println("printFrequency "+printFrequency);
        int numAfterConnected = 0;
        
        for (loop = 0; loop < numIterations; loop++){
            //System.out.println(loop);
            if (loop % printFrequency == 0 && !connected){
                System.out.println(loop);
                print(color, Integer.toString(loop));
            }
            if (connected){
                System.out.println("connected "+numAfterConnected);
                if (numAfterConnected % 10 == 0) {
                    System.out.println(loop);
                    print(color, Integer.toString(loop));
                }
                numAfterConnected++;
                if (numAfterConnected > 1000) break;
            }

            //choose stimulus point
            int[] p = chooseStimulusPoint();

            int stim_x = p[0];
            int stim_y = p[1];
            
            //invade zero
            int dir = selectNext(stim_x, stim_y, 0);
            if (dir == -1) continue;
            grid[stim_x + dx[dir]][stim_y + dy[dir]] = 2;
            color[stim_x + dx[dir]][stim_y + dy[dir]] = color[stim_x][stim_y];
            grid[stim_x][stim_y ] = 0;
            color[stim_x][stim_y] = 0;

            //replace all 1s with 2s
            for (int i = 0; i < gridSize; i++){
                for (int j = 0; j < gridSize; j++){
                    if (grid[i][j] == 1){
                        grid[i][j] = 2;
                    }
                }
            }
            
            //swap bubble with neighbors
            int bX = stim_x;
            int bY = stim_y;
            for (int numMoves = 0; numMoves < maxNumMoves; numMoves++){
                visited[bX][bY] = true;
                int numBubbles = 0;
                for (int i = 0; i < 4; i++){
                    if (bX + dx[i] < 0 || bY + dy[i] < 0 || bX + dx[i] >= gridSize || bY + dy[i] >= gridSize){
                        numBubbles++;
                        continue;
                    }
                    if (grid[bX + dx[i]][bY + dy[i]] == 0) numBubbles++;
                }
                if (numBubbles >= maxBubbleNeighbors) break;

                dir = selectNext(bX, bY, 2);
                if (dir == -1) break;
                color[bX][bY] = color[bX + dx[dir]][bY + dy[dir]];
                grid[bX][bY] = 2;
                bX += dx[dir];
                bY += dy[dir];
                grid[bX][bY] = 0;
                color[bX][bY] = 0;
            }
            
            //reassign grid
            for (int i = 1; i < gridSize - 1; i++){
                for (int j = 1; j < gridSize - 1; j++){
                    int numZeros = 0;
                    for (int k = 0; k < 4; k++){
                        if (grid[i + dx[k]][j + dy[k]] == 0) numZeros++;
                    }
                    if (numZeros == 0 && grid[i][j] != 0){
                        grid[i][j] = 1;
                    }
                }
            }
            reset(visited);
            if (!connected) {
                checkConnected();
            }

        }
        out.close();

    }
    
    //check if the two cells have fused yet
    static void checkConnected() throws IOException {
        for (int i = 1; i < gridSize - 1; i++){
            for (int j = 1; j < gridSize - 1; j++){
                for (int k = 0; k < 4; k++){
                    if (color[i][j] == 0 || color[i + dx[k]][j + dy[k]] == 0) continue;
                    if (color[i][j] != color[i + dx[k]][j + dy[k]]){
                        connected = true;
                        System.out.println("connected for the first time "+loop);
                        print(color, Integer.toString(loop)+"_FIRST_CONNECTION");
                        break;
                    }
                }
            }
        }
    }
    
    //print out some parameter info to .txt file
    static void dumpData(){
        out.println("running normal_model_double.java");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        out.println("current time and date "+dtf.format(now));
        out.println("grid size "+gridSize);
        out.println("cell size "+cellSize);

    }
    
    //create image
    static void print(int[][] arr, String iteration) throws IOException {
        int height = picMagnifier * arr.length;
        int width = picMagnifier * arr[0].length;
        int[] pixels = new int[width*height];
        // Рисуем диагональ.


        for (int j = 0; j < arr.length; j++) {
            for (int i = 0; i < arr[0].length; i++) {
                color(pixels, i, j, map[arr[i][j]]);
            }
        }
        String fileName = path +"pic_"+ iteration+".png";
        num++;
        BufferedImage pixelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixelImage.setRGB(0, 0, width, height, pixels, 0, width);
        File outputfile = new File(fileName);
        ImageIO.write(pixelImage, "png", outputfile);
    }
    
    //get color of each pixel, helper for print function
    static void color(int[] pixels, int x, int y, int color){
        for (int i = 0; i < picMagnifier; i++){
            for (int j = 0; j < picMagnifier; j++){
                int nX = picMagnifier * x + i;
                int nY = picMagnifier * y + j;
                pixels[nY * picMagnifier* grid[0].length + nX] = color;
            }
        }
    }
    //reset the visited array
    static void reset(boolean[][] visit){
        for (int i = 0; i < visit.length; i++) Arrays.fill(visit[i], false);
    }
    static int[] chooseStimulusPoint(){
        int numPossible = 0;
        for (int i = 0; i < gridSize; i++){
            for (int j = 0; j < gridSize; j++){
                if (grid[i][j] == 2) numPossible++;
            }
        }
        int r = (int) (Math.random() * numPossible);
        int c = 0;
        for (int i = 0; i < gridSize; i++){
            for (int j = 0; j < gridSize; j++){
                if (grid[i][j] != 2) continue;
                if (c == r){
                    return new int[] {i, j};
                }
                c++;

            }
        }
        return new int[] {-1, -1};
    }
    
    //select the next stimulus point
    static int selectNext(int bX, int bY, int needed){
        int dir = (int) (Math.random() * 4);
        boolean[] tried = new boolean[4];
        while (bX + dx[dir] < 0 || bY + dy[dir] < 0 || bX + dx[dir] >= gridSize || bY + dy[dir] >= gridSize || grid[bX + dx[dir]][bY + dy[dir]] != needed || visited[bX + dx[dir]][bY + dy[dir]]){
            tried[dir] = true;
            if (allTrue(tried)){
                return -1;
            }
            dir = (int) (Math.random() * 4);
        }
        return dir;
    }
    
    //spawn cell at (spawnX, spawnY) with ID id
    static void spawn(int spawnX, int spawnY, int id){
        out.println("spawn at "+spawnX+", "+spawnY);
        for (int i = 0; i <= cellSize/2; i++){
            grid[spawnX + i][-cellSize/2 + i + spawnY] = 2;
            grid[spawnX + i][cellSize/2 - i + spawnY] = 2;
            grid[spawnX - i][-cellSize/2 + i + spawnY] = 2;
            grid[spawnX - i][cellSize/2 - i + spawnY] = 2;
            color[spawnX + i][-cellSize/2 + i + spawnY] =id;
            color[spawnX + i][cellSize/2 - i + spawnY] = id;
            color[spawnX - i][-cellSize/2 + i + spawnY] =id;
            color[spawnX - i][cellSize/2 - i + spawnY] = id;
            for (int j = -cellSize/2 + i + spawnY + 1; j <= cellSize/2 - i + spawnY - 1; j++) {
                grid[spawnX + i][j] = 1;
                grid[spawnX - i][j] = 1;
                color[spawnX + i][j] = id;
                color[spawnX - i][j] = id;
            }
        }
    }
    
    //check if entire array is true
    static boolean allTrue(boolean[] f){
        for (boolean i : f){
            if (!i) return false;
        }
        return true;
    }
    
    static class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        public FastScanner(InputStream stream) {
            br = new BufferedReader(new InputStreamReader(stream));
            st = new StringTokenizer("");
        }

        public FastScanner(String fileName) throws Exception {
            br = new BufferedReader(new FileReader(new File(fileName)));
            st = new StringTokenizer("");
        }

        public String next() throws Exception {
            while (!st.hasMoreTokens()) {
                st = new StringTokenizer(br.readLine());
            }
            return st.nextToken();
        }

        public int nextInt() throws Exception {
            return Integer.parseInt(next());
        }

        public long nextLong() throws Exception {
            return Long.parseLong(next());
        }

        public Double nextDouble() throws Exception {
            return Double.parseDouble(next());
        }

        public String nextLine() throws Exception {
            if (st.hasMoreTokens()) {
                StringBuilder str = new StringBuilder();
                boolean first = true;
                while (st.hasMoreTokens()) {
                    if (first) {
                        first = false;
                    } else {
                        str.append(" ");
                    }
                    str.append(st.nextToken());
                }
                return str.toString();
            } else {
                return br.readLine();
            }
        }
    }
}

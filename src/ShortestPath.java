import java.util.*;

class Cello {
    int row;
    int col;
    public Cello(int rowIndex, int colIndex) {
        super();
        this.row = rowIndex;
        this.col = colIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cello cell = (Cello) o;
        return row == cell.row &&
                col == cell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}



public class ShortestPath {

    private static List<int[]> shortestPath(char[][] grid) {
        ArrayList<int[]> path = new ArrayList<>();
        for (int i = 0; i < grid.length; ++i) {
            for (int j = 0; j < grid[0].length; ++j) {
                if (grid[i][j] == 'D') {
                    bfs(grid, new Cello(i, j), path);
                }
            }
        }
        return path;
    }

    public static void main(String[] args) {
        char[][] grid2 = new char[35][35] ;

        for (int i = 0 ; i < 35 ; i++) {
            for (int j = 0 ; j < 35 ; j++) {
                grid2[i][j] = '1';
            }
        }

        grid2[1-1][2-1] = 'D';
        grid2[3-1][1-1] = 'S';
        grid2[2-1][2-1] = '0';
        grid2[1-1][1-1] = '0';



        List<int[]> path = shortestPath(grid2);

        System.out.println("Path length:" + (path.size() - 1));
        path.stream().forEach(i -> {
            System.out.println("{" + i[0] + "," + i[1] + "}");
        });

        int[] banana = path.get(1);

        System.out.println(banana[0] + "," + banana[1]);
    }

    public static Position move(Estrategia e, Position jogador) {

        char[][] mapa = new char[35][35] ;

        for (int i = 0 ; i < 35 ; i++) {
            for (int j = 0 ; j < 35 ; j++) {
                mapa[i][j] = '1';
            }
        }
        mapa[jogador.getX()-1][jogador.getY()-1] = 'S';
        mapa[e.getPosition().getX()-1][e.getPosition().getY()-1] = 'D';

        ArrayList<Position> bloqueado = e.getPos_Inimigos();

        for (Position b : bloqueado) {
            mapa[b.getX()-1][b.getY()-1] = '0';
        }

    /*    System.out.println(  "---- MAPA ----");
        for (int i = 0 ; i < 35 ; i++) {
            for (int j = 0 ; j < 35 ; j++) {
                System.out.print(mapa[i][j]);
            }
            System.out.println("\n");
        } */

        List<int[]> caminho = shortestPath(mapa);

        System.out.println("ENTRA AQUI E NAO FAZ");

        caminho.stream().forEach(i -> {
            System.out.println("{" + i[0] + "," + i[1] + "}");
        });

        if(caminho.size()>1) {
            int[] novaPosicao = caminho.get(1);

            Position res = new Position(novaPosicao[0] + 1, novaPosicao[1] + 1);
            return res;
        } else {
            return jogador;
        }
    }

    private static void bfs(char[][] grid, Cello start, List<int[]> path) {

        int[] xDirs = new int[] {0,0,1, -1};
        int[] yDirs = new int[] {1,-1, 0, 0};

        Queue<Cello> bfsQueue = new LinkedList<>();
        bfsQueue.add(start);
        HashMap<Cello, Cello> parentMap = new HashMap<>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        Cello endCell = null;
        while(!bfsQueue.isEmpty()) {
            boolean flag = false;
            Cello from = bfsQueue.poll();

            for (int k = 0; k < xDirs.length; ++k) {
                int nextX = from.row + xDirs[k];
                int nextY = from.col + yDirs[k];

                if (nextX < 0 || nextX >= grid.length || nextY < 0
                        || nextY >= grid[0].length || grid[nextX][nextY] == '0'
                        || visited[nextX][nextY]) {
                    continue;
                }

                visited[nextX][nextY] = true;
                Cello nextCell = new Cello(nextX, nextY);
                bfsQueue.add(nextCell);
                //we need a way to determine from where we have reached here
                //storing the child to parent mapping, this will be used to retrieve the entire path
                parentMap.put(nextCell, from);
                //if (grid[nextX][nextY] == 'E')
                if (grid[nextX][nextY] == 'D') {
                    endCell = new Cello(nextX, nextY);
                    flag = true;
                    break;
                }
            }
            if (flag) {
                break;
            }
        }
        Stack<Cello> stack = new Stack<>();
        stack.push(endCell);

        //build the path from destination to source
        while (true) {
            Cello fromCell = parentMap.get(endCell);
            stack.push(fromCell);
            if (fromCell == start) break;
            endCell = fromCell;
        }
        //reverse the above path and convert as List<int[]>
        while (!stack.isEmpty()) {
            Cello p = stack.pop();
            path.add(new int[] {p.row, p.col});
        }
    }


}

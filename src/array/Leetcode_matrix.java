package array;

/**
 * 螺旋矩阵
 * Created by Nelson on 2020/5/12.
 */
public class Leetcode_matrix {

    /**
     * 面试题29. 顺时针打印矩阵
     * https://leetcode-cn.com/problems/shun-shi-zhen-da-yin-ju-zhen-lcof/
     *
     * @param matrix
     * @return
     */
    public int[] spiralOrder(int[][] matrix) {
        if (matrix.length == 0) {
            return new int[0];
        }

        int l = 0, r = matrix[0].length - 1, t = 0, b = matrix.length - 1, x = 0;
        int[] res = new int[(r + 1) * (b + 1)];
        while (true) {
            for (int i = l; i <= r; i++) {
                res[x++] = matrix[t][i]; // left -> right
            }

            if (++t > b) {
                break;
            }

            for (int i = t; i <= b; i++) {
                res[x++] = matrix[i][r]; // top -> bottom
            }

            if (l > --r) {
                break;
            }

            for (int i = r; i >= l; i--) {
                res[x++] = matrix[b][i]; // right -> left
            }

            if (t > --b) {
                break;
            }
            for (int i = b; i >= t; i--) {
                res[x++] = matrix[i][l]; // bottom -> top
            }

            if (++l > r) {
                break;
            }
        }
        return res;
    }

    /**
     * 59. 螺旋矩阵 II
     * https://leetcode-cn.com/problems/spiral-matrix-ii/
     *
     * @param n
     * @return
     */
    public int[][] generateMatrix(int n) {
        int l = 0, r = n - 1, t = 0, b = n - 1;
        int[][] mat = new int[n][n];
        int num = 1, tar = n * n;
        while (num <= tar) {
            for (int i = l; i <= r; i++) {
                mat[t][i] = num++;
            }

            t++;

            for (int i = t; i <= b; i++) {
                mat[i][r] = num++;
            }

            r--;


            for (int i = r; i >= l; i--) {
                mat[b][i] = num++;
            }

            b--;

            for (int i = b; i >= t; i--) {
                mat[i][l] = num++;
            }

            l++;
        }
        return mat;
    }

}
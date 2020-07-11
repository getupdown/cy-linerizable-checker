package cn.cy.network;

class TreeNode {
    int value;
    TreeNode left;
    TreeNode right;

    public TreeNode(int value) {
        this.value = value;
    }

    public TreeNode(int value, TreeNode left, TreeNode right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }
}

public class Demo {

    public static void main(String[] args) {

        TreeNode root = new TreeNode(4);
        root.left = new TreeNode(2);
        root.left.right = new TreeNode(3);
        root.left.left = new TreeNode(1);
        root.right = new TreeNode(5);

        dfs(root);

    }

    public static void dfs(TreeNode node) {
        if (node == null) {
            return;
        }

        dfs(node.left);
        System.out.println(node.value);
        dfs(node.right);

    }

}

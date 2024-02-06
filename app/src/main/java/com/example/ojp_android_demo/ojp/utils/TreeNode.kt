package com.example.ojp_android_demo.ojp.utils

class TreeNode(
    val name: String,
    val parentName: String?,
    val attributes: Map<String, String>,
    val children: MutableList<TreeNode>,
    var text: String?
) {

    fun findTextFromChildNamed(expr: String): String? {
        val exprParts = expr.split("/")

        var contextNode: TreeNode? = this
        for (nodeName in exprParts) {
            contextNode = contextNode?.findChildNamed(nodeName)
        }

        return contextNode?.text
    }

    fun findChildNamed(expr: String): TreeNode? {
        val exprParts = expr.split("/")

        var contextNode: TreeNode? = this
        for (nodeName in exprParts) {
            contextNode = contextNode?.children?.find { it.name == nodeName }
        }

        return contextNode
    }

    fun findChildrenNamed(name: String): List<TreeNode> {
        if (name.contains("/")) {
            println("ERROR - do you want to use more than one level for findChildrenNamed? Use findTextFromChildNamed.")
            println(name)
        }

        return children.filter { it.name == name }
    }

    fun computeText(): String? {
        val textParts = mutableListOf<String>()
        if (this.text == null) {
            if (this.children.isEmpty()) {
                return null
            }
            for (child in this.children) {
                val childText = child.computeText()
                if (childText != null) {
                    textParts.add(childText)
                }
            }
        } else {
            val nodeText = this.text;
            if (nodeText != null) {
                textParts.add(nodeText)
            }
        }

        return textParts.joinToString(" ")
    }
}
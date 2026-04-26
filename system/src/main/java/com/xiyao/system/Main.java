package com.xiyao.system;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        //TIP 当文本光标位于高亮显示的文本处时按 <shortcut actionId="ShowIntentionActions"/>
        // 查看 IntelliJ IDEA 建议如何修正。
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP 按 <shortcut actionId="Debug"/> 开始调试代码。我们已经设置了一个 <icon src="AllIcons.Debugger.Db_set_breakpoint"/> 断点
            // 但您始终可以通过按 <shortcut actionId="ToggleLineBreakpoint"/> 添加更多断点。
            System.out.println("i = " + i);
        }
    }
}

// 用户管理：用户是系统操作者，该功能主要完成系统用户配置。
// 角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分。
// 菜单管理：配置系统菜单，操作权限，按钮权限标识等。

// 字典管理：对系统中经常使用的一些较为固定的数据进行维护。
// 参数管理：对系统动态配置常用参数。

// 操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询。
// 登录日志：系统登录日志记录查询包含登录异常。

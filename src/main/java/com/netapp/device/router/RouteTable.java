package com.netapp.device.router;

import com.netapp.device.Iface;
import com.netapp.packet.IPv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteTable {

    private List<RouteEntry> entries;

    public RouteTable() { this.entries = new LinkedList<RouteEntry>(); }

    /**
     * 向路由表中插入一条条目。
     * @param dstIp 目标 IP
     * @param gwIp 网关 IP
     * @param maskIp 子网掩码
     * @param iface 通过该接口发送数据包以到达目标或网关
     */
    public void insert(String dstIp, String gwIp, String maskIp, Iface iface) {
        RouteEntry entry = new RouteEntry(dstIp, gwIp, maskIp, iface);
        synchronized (this.entries) {
            this.entries.add(entry);
        }
    }

    /**
     * 从路由表中删除一条条目。
     * @param dstIp 要删除的条目的目标 IP
     * @param maskIp 要删除的条目的子网掩码
     * @return 如果找到并删除了匹配的条目则返回 true，否则返回 false
     */
    public boolean remove(String dstIp, String maskIp) {
        synchronized (this.entries) {
            RouteEntry entry = this.find(dstIp, maskIp);
            if (null == entry) {
                return false;
            }
            this.entries.remove(entry);
        }
        return true;
    }

    /**
     * 查找与给定 IP 地址匹配的路由条目。
     * @param ipAddress IP 地址
     * @return 匹配的路由条目，如果不存在则返回 null
     */
    public RouteEntry lookup(String ipAddress) {

        int ip = IPv4.toIPv4Address(ipAddress);

        synchronized (this.entries) {
            /*****************************************************************/
            /* 找到具有最长前缀匹配的路由条目                         */

            // 初始化最佳匹配为null
            RouteEntry bestMatch = null;
            // 初始化默认匹配为null
            RouteEntry defaultMatch = null;

            // 遍历所有路由条目
            for (RouteEntry entry : this.entries) {

                // 暂时忽略默认网关
                if(entry.getDestinationAddress().equals(IPv4.DEFAULT_IP) &&
                        entry.getMaskAddress().equals(IPv4.DEFAULT_IP))
                {
                    defaultMatch = entry;
                    continue;
                }

                // 使用路由条目的掩码对目标IP进行掩码操作
                int maskedDst = ip &
                        IPv4.toIPv4Address(entry.getMaskAddress());
                // 获取路由条目的子网地址
                int entrySubnet = IPv4.toIPv4Address(entry.getDestinationAddress()) &
                        IPv4.toIPv4Address(entry.getMaskAddress());

                // 如果掩码后的目标IP与子网地址匹配
                if (maskedDst == entrySubnet) {
                    // 如果当前匹配是第一个或者当前路由条目的掩码更长
                    if ((bestMatch == null) ||
                            (IPv4.toIPv4Address(entry.getMaskAddress()) >
                                    IPv4.toIPv4Address(bestMatch.getMaskAddress()))) {
                        bestMatch = entry; // 更新最佳匹配
                    }
                }
            }

            // 如果找不到最佳匹配， 则最佳匹配就是默认匹配
            if(bestMatch == null){
                bestMatch = defaultMatch;
                System.out.println("Can't find best match, best match set default match: " + defaultMatch);
            }

            return bestMatch; // 返回最佳匹配的路由条目

            /*****************************************************************/
        }
    }


    /**
     * 在路由表中查找一条条目。
     * @param dstIp 要查找的条目的目标 IP
     * @param maskIp 要查找的条目的子网掩码
     * @return 如果找到匹配的条目则返回该条目，否则返回 null
     */
    private RouteEntry find(String dstIp, String maskIp) {
        synchronized (this.entries) {
            for (RouteEntry entry : this.entries) {
                if ((Objects.equals(entry.getDestinationAddress(), dstIp)) && (Objects.equals(entry.getMaskAddress(), maskIp))) {
                    return entry;
                }
            }
        }
        return null;
    }


    @Override
    public String toString() {
        synchronized (this.entries) {
            if (0 == this.entries.size()) {
                return " 警告：路由表为空";
            }

            String result = "目标IP\t\t网关IP\t\t子网掩码\t\t\t接口\n";
            for (RouteEntry entry : entries) {
                result += entry.toString() + "\n";
            }
            return result;
        }
    }

    /**
     * 从文件中加载静态路由表填充路由表。
     * @param filename 包含静态路由表的文件的名称
     * @param router 与路由表相关联的路由器
     * @return 如果成功加载路由表则返回 true，否则返回 false
     */
    public boolean load(String filename, Router router) {
        // 打开文件
        BufferedReader reader;
        try {
            FileReader fileReader = new FileReader(filename);
            reader = new BufferedReader(fileReader);
        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
            return false;
        }

        while (true) {
            // 从文件读取路由表项
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.err.println(e.toString());
                try {
                    reader.close();
                } catch (IOException f) {
                }
                return false;
            }

            // 如果已经到达文件末尾，则停止
            if (null == line) {
                break;
            }

            // 解析用于路由表项的字段
            String ipPattern = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
            String ifacePattern = "([a-zA-Z0-9_]+)";
            Pattern pattern = Pattern.compile(String.format(
                    "%s\\s+%s\\s+%s\\s+%s",
                    ipPattern, ipPattern, ipPattern, ifacePattern));
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches() || matcher.groupCount() != 4) {
                System.err.println("路由表文件中存在无效条目");
                System.out.println(line);
                try {
                    reader.close();
                } catch (IOException f) {
                }
                return false;
            }

            String dstIp = matcher.group(1);
            if (null == dstIp) {
                System.err.println("加载路由表时出错，无法将 "
                        + matcher.group(1) + " 转换为有效的 IP");
                try {
                    reader.close();
                } catch (IOException f) {
                }
                return false;
            }

            String gwIp = matcher.group(2);

            String maskIp = matcher.group(3);
            if (null == maskIp) {
                System.err.println("加载路由表时出错，无法将 "
                        + matcher.group(3) + " 转换为有效的 IP");
                try {
                    reader.close();
                } catch (IOException f) {
                }
                return false;
            }

            String ifaceName = matcher.group(4).trim();
            Iface iface = router.getInterface(ifaceName);
            if (null == iface) {
                System.err.println("加载路由表时出错，无效的接口 "
                        + matcher.group(4));
                try {
                    reader.close();
                } catch (IOException f) {
                }
                return false;
            }

            // 将条目添加到路由表
            this.insert(dstIp, gwIp, maskIp, iface);
        }

        // 关闭文件
        try {
            reader.close();
        } catch (IOException f) {
        }

        return true;

    }

}

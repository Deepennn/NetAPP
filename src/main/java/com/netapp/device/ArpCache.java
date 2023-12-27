package com.netapp.device;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MAC地址到IP地址映射的缓存。
 */
public class ArpCache
{
    /** 缓存中的条目；将IP地址映射到一个条目 */
    private Map<String, ArpEntry> entries;

    /**
     * 为路由器初始化一个空的ARP缓存。
     */
    public ArpCache()
    { this.entries = new ConcurrentHashMap<String, ArpEntry>(); }

    /**
     * 在ARP缓存中插入指定IP地址和MAC地址对应的条目。
     * @param ip MAC地址对应的IP地址
     * @param mac IP地址对应的MAC地址
     */
    public void insert(String ip, String mac)
    {
        this.entries.put(ip, new ArpEntry(ip, mac));
    }

    /**
     * 检查缓存中是否存在IP到MAC的映射。
     * @param ip 希望获取其MAC地址的IP地址
     * @return 缓存中的IP到MAC映射；如果不存在则返回null
     */
    public ArpEntry lookup(String ip)
    { return this.entries.get(ip); }


    public String toString()
    {
        String result = "IP\t\t\tMAC\n";
        for (ArpEntry entry : this.entries.values())
        { result += entry.toString() + "\n"; }
        return result;
    }

    /**
     * 从文件中加载ARP缓存。
     * @param filename 包含ARP缓存的文件的名称
     * @return 如果ARP缓存成功加载，则返回true；否则返回false
     */
    public boolean load(String filename)
    {
        // 打开文件
        BufferedReader reader;
        try
        {
            FileReader fileReader = new FileReader(filename);
            reader = new BufferedReader(fileReader);
        }
        catch (FileNotFoundException e)
        {
            System.err.println(e.toString());
            return false;
        }

        while (true)
        {
            // 从文件中读取ARP条目
            String line = null;
            try
            { line = reader.readLine(); }
            catch (IOException e)
            {
                System.err.println(e.toString());
                try { reader.close(); } catch (IOException f) {};
                return false;
            }

            // 如果已经到达文件末尾，则停止
            if (null == line)
            { break; }

            // 解析ARP条目的字段
            String ipPattern = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
            String macByte = "[a-fA-F0-9]{2}";
            String macPattern = "("+macByte+":"+macByte+":"+macByte
                    +":"+macByte+":"+macByte+":"+macByte+")";
            Pattern pattern = Pattern.compile(String.format(
                    "%s\\s+%s", ipPattern, macPattern));
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches() || matcher.groupCount() != 2)
            {
                System.err.println("ARP缓存文件中存在无效条目");
                try { reader.close(); } catch (IOException f) {};
                return false;
            }

            String ip = matcher.group(1);
            if (null == ip)
            {
                System.err.println("加载ARP缓存时出错，无法将 "
                        + matcher.group(1) + " 转换为有效的IP");
                try { reader.close(); } catch (IOException f) {};
                return false;
            }

           String mac = null;
            try
            { mac = matcher.group(2); }
            catch(IllegalArgumentException iae)
            {
                System.err.println("加载ARP缓存时出错，无法将 "
                        + matcher.group(2) + " 转换为有效的MAC");
                try { reader.close(); } catch (IOException f) {};
                return false;
            }

            // 向ARP缓存添加条目
            this.insert(ip,mac);
        }

        // 关闭文件
        try { reader.close(); } catch (IOException f) {};

        return true;
    }


}

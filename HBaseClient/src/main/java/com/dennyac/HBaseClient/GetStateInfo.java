package com.dennyac.HBaseClient;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class GetStateInfo {
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		HBaseConnection hb = new HBaseConnection("all_countries_hash");
		HTable table = hb.getTable();

		// Get 15 nearest neighbors
		Filter filter = new PageFilter(15);
		ResultScanner scanner = null;
		Scan scan;
		HashMap<String, Integer> topState;

		String[] fields = null;
		String cc;
		FileUtils fu = new FileUtils("/home/ec2-user/denny/tmp_choropleth_data_us");
		File writeFile = new File(
				"/home/ec2-user/denny/predicted_states/artist_states.tsv");
		if (!writeFile.exists()) {
			writeFile.createNewFile();
		}
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		int max;
		String topC = null;
		for (String file : fu.listFiles()) {

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {

				fields = line.split("\\t");

				try {

					scan = new Scan(Bytes.toBytes(fields[2].substring(0, 24)));
					scan.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("a1"));
					scan.setFilter(filter);
					scanner = table.getScanner(scan);

					// mainObj.put(year, yearTopTracks);
					topState = new HashMap<String, Integer>();
					for (Result res : scanner) {
						cc = Bytes.toString(res.value());
						topState
								.put(cc,
										topState.containsKey(cc) ? topState
												.get(cc) + 1 : 1);

					}
					max = 0;

					for (Entry<String, Integer> entry : topState.entrySet()) {
						if (entry.getValue() > max) {
							topC = entry.getKey();
							max = entry.getValue();
						}
					}

					bw.write(line + "\t" + topC);
					bw.newLine();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			br.close();
		}
		bw.close();
		hb.disconnectHBase();
	}

}

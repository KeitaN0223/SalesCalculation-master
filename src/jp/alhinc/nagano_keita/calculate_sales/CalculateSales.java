package jp.alhinc.nagano_keita.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		// 支店のマップを宣言
		HashMap<String, String> branchMap = new HashMap<String, String>();

		// 商品定義のマップを宣言
		HashMap<String, String> commodityMap = new HashMap<String, String>();

		// 支店ごとの売上マップを宣言する
		HashMap<String, Long> branchEarningsMap = new HashMap<String, Long>();

		// 商品ごとの売上マップを宣言する
		HashMap<String, Long> commodityEarningsMap = new HashMap<String, Long>();
		// 1.1 売上集計フォルダから支店定義ファイルがあるか参照する
		File branchFile = new File(args[0], "branch.lst");

		// 1.1 売上集計フォルダから支店定義ファイルがあるか参照する
		if (!branchFile.exists()) {
			System.out.println("支店定義ファイルが存在しません");
			return;
		}

		// 1.2 支店定義ファイルのフォーマットが正しいのか
		BufferedReader br = null;
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		try {
			FileReader fr;
			fr = new FileReader(branchFile);
			br = new BufferedReader(fr);
			String s;
			// 1.2 ファイルの読み込み
			while ((s = br.readLine()) != null) {
				String[] resultArray = s.split(",");
				// 1.2 支店番号が不正のとき、エラーメッセージを送る
				if (resultArray.length != 2) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				if (!resultArray[0].matches("^[0-9]{3}$")) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				// 1.2 支店番号と支店名を紐付けた
				branchMap.put(resultArray[0], resultArray[1]);
				// 支店番号に紐づいている支店別売上を0円に初期化
				branchEarningsMap.put(resultArray[0], 0L);
			}

		} catch (IOException e) {
			System.out.println("IOException型の例外をキャッチしました");
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				return;
			}
		}

		// System.out.println(branchEarningsMap.entrySet());

		// 2.1 商品定義ファイルがあるか参照する
		File commodityFile = new File(args[0], "commodity.lst");

		// 2.1 商品定義ファイルがあるか参照する
		if (!commodityFile.exists()) {
			System.out.println("商品定義ファイルが存在しません");
			return;
		}

		try {
			// 2.2 商品定義ファイルのフォーマットが正しいのか
			FileReader fr1;
			fr1 = new FileReader(commodityFile);
			br1 = new BufferedReader(fr1);
			String s1;

			// 2.2 商品定義ファイルのフォーマットが正しいのか
			while ((s1 = br1.readLine()) != null) {
				String[] resultArray1 = s1.split(",");
				if (resultArray1.length != 2) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				if (!resultArray1[0].matches("^[a-z0-9A-Z]{8}$")) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				// 2.2 商品番号と商品を紐付けた
				commodityMap.put(resultArray1[0], resultArray1[1]);
				// 商品番号に紐づいている商品別売上を0円に初期化
				commodityEarningsMap.put(resultArray1[0], 0L);
			}

		} catch (IOException e) {
			System.out.println("IOException型の例外をキャッチしました");
			return;
		} finally {
			try {
				br1.close();
			} catch (IOException e) {
				return;
			}
		}
		// 3.1 ファイル名半角数字8桁のrcdファイルがあるか参照する
		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		// フォルダーがないか調べる あったらエラーを送る
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		ArrayList<String> rcdFiles = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String fileName = file.getName();
			if (fileName.matches("^[0-9]{7}[1-9].rcd$")) {
				// 3.1 rcdファイルをrcdFilesに格納した
				rcdFiles.add(fileName);
			}
		}
		// rcdファイルの歯抜けを調べる
		ArrayList<Integer> rcdNum = new ArrayList<Integer>(); // rcdファイルの番号が格納されているArraylist
		for (int i = 0; i < rcdFiles.size(); i++) {
			rcdNum.add(Integer.parseInt(rcdFiles.get(i).substring(0, 8)));
		} // rcdファイルの番号をrcdNumに格納した

		int max = rcdNum.get(0); // rcd番号の最大値と最小値を調べる
		int min = rcdNum.get(1);
		for (int i = 0; i < rcdNum.size(); i++) {
			int j = rcdNum.get(i);
			if (j > max) {
				max = j;
			}
			if (j < min) {
				min = j;
			}
		}
		int fileNum = max - min + 1; // rcdファイルの数
		if (fileNum != rcdFiles.size()) {
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		DecimalFormat dformat = new DecimalFormat("00000000");

		try {
			// rcdFiles内をソートする
			Collections.sort(rcdFiles);
			for (int i = 1; i < rcdFiles.size() + 1; i++) {
				// ArrayListでrcdDataをまとめる
				ArrayList<String> rcdData = new ArrayList<String>();

				File rcdFilesPath = new File(args[0], rcdFiles.get(i - 1));
				br2 = new BufferedReader(new FileReader(rcdFilesPath));
				String str;
				// 行ごとのデータをArrayListに格納した
				while ((str = br2.readLine()) != null) {
					rcdData.add(str);
				}
				br2.close();
				if (rcdData.size() != 3) {
					System.out.println(dformat.format(i) + ".rcdのフォーマットが不正です");
					return;
				}

				// rcdDataの1番目の要素(支店コード)が不正だったらエラーを出力し、終了

				if (branchMap.get(rcdData.get(0)) == null) {
					System.out.println(dformat.format(i) + ".rcdの支店コードが不正です");
					return;
				}
				// rcdDataの2番目の要素(商品コード)が不正だったらエラーを出力し、終了
				if (commodityMap.get(rcdData.get(1)) == null) {
					System.out.println(dformat.format(i) + ".rcdの商品コードが不正です");
					return;
				}
				// System.out.println(rcdData);

				// rcdDataの一番目の要素（支店コード）から売上を呼び出す
				// branchBaseMoneyはもとのお金
				long branchBaseMoney = branchEarningsMap.get(rcdData.get(0));
				// moneyはrcdData三番目の要素（売上）
				long money = Long.parseLong(rcdData.get(2));
				// money(売上)が10桁超えたらエラー処理
				if (money > 9999999999L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				// branchSumは合計額
				long branchSum = branchBaseMoney + money;
				// branchSum(支店別売上)が10桁超えたらエラー処理
				if (branchSum > 9999999999L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchEarningsMap.put(rcdData.get(0), branchSum);

				// rcdDataの二番目の要素（商品コード）から売上を呼び出す
				// commodityBaseMoneyはもとのお金
				long commodityBaseMoney = commodityEarningsMap.get(rcdData.get(1));
				// moneyは先ほどのものを使いまわす
				// commoditySumは合計額
				long commoditySum = commodityBaseMoney + money;
				// commoditySum(商品別売上)が10桁超えたらエラー処理
				if (commoditySum > 9999999999L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				commodityEarningsMap.put(rcdData.get(1), commoditySum);
			}
			// 4 Mapのvalue値でソートしてファイルに出力 要復習
			List<Map.Entry<String, Long>> branchEntries = new ArrayList<Map.Entry<String, Long>>(
					branchEarningsMap.entrySet());
			Collections.sort(branchEntries, new Comparator<Map.Entry<String, Long>>() {
				@Override
				public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
				}
			});
			File branchOutFile = new File(args[0], "branch.out");
			FileWriter fw = new FileWriter(branchOutFile);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for (Entry<String, Long> s : branchEntries) {
				pw.println(s.getKey() + "," + branchMap.get(s.getKey()) + "," + Long.toString(s.getValue()));
			}
			pw.close();

			// 4 ソートしてcommodity.outの出力
			List<Map.Entry<String, Long>> commodityEntries = new ArrayList<Map.Entry<String, Long>>(
					commodityEarningsMap.entrySet());
			Collections.sort(commodityEntries, new Comparator<Map.Entry<String, Long>>() {
				@Override
				public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
				}
			});
			File commodityOutFile = new File(args[0], "commodity.out");
			FileWriter cfw = new FileWriter(commodityOutFile);
			BufferedWriter cbw = new BufferedWriter(cfw);
			PrintWriter cpw = new PrintWriter(cbw);
			for (Entry<String, Long> s : commodityEntries) {
				cpw.println(s.getKey() + "," + commodityMap.get(s.getKey()) + "," + Long.toString(s.getValue()));
			}
			cpw.close();
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				br2.close();
			} catch (IOException e) {
				return;
			}
		}
	}

}

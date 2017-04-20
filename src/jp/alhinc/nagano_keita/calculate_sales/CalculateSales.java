package jp.alhinc.nagano_keita.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
		try {
			br = new BufferedReader(new FileReader(branchFile));
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
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
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
			br = new BufferedReader(new FileReader(commodityFile));
			String s1;

			// 2.2 商品定義ファイルのフォーマットが正しいのか
			while ((s1 = br.readLine()) != null) {
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
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
		// 3.1 ファイル名半角数字8桁のrcdファイルがあるか参照する
		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		// フォルダーがないか調べる あったらエラーを送る
		/*
		 * for (int i = 0; i < files.length; i++) { if (files[i].isDirectory())
		 * { System.out.println("売上ファイル名が連番になっていません"); return; } }
		 */
		ArrayList<String> rcdFiles = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String fileName = file.getName();
			if (fileName.matches("^[0-9]{8}.rcd$") && file.isFile()) {
				// 3.1 rcdファイルをrcdFilesに格納した
				rcdFiles.add(fileName);
			}
		}
		// rcdファイルの歯抜けを調べる
		ArrayList<Integer> rcdNum = new ArrayList<Integer>(); // rcdファイルの番号が格納されているArraylist
		for (int i = 0; i < rcdFiles.size(); i++) {
			rcdNum.add(Integer.parseInt(rcdFiles.get(i).substring(0, 8)));
		} // rcdファイルの番号をrcdNumに格納した
			// rcdFiles内をソートする
		Collections.sort(rcdFiles);
		int min = Integer.parseInt(rcdFiles.get(0).substring(0, 8));
		int max = Integer.parseInt(rcdFiles.get(rcdFiles.size() - 1).substring(0, 8));
		int fileNum = max - min + 1; // rcdファイルの数
		if (fileNum != rcdFiles.size()) {
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}
		for (int i = 0; i < rcdFiles.size(); i++) {
			// ArrayListでrcdDataをまとめる
			ArrayList<String> rcdData = new ArrayList<String>();
			try {
				File rcdFilesPath = new File(args[0], rcdFiles.get(i));
				br = new BufferedReader(new FileReader(rcdFilesPath));
				String str;
				// 行ごとのデータをArrayListに格納した
				while ((str = br.readLine()) != null) {
					rcdData.add(str);
				}

			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}

			if (rcdData.size() != 3) {
				System.out.println(rcdFiles.get(i) + "のフォーマットが不正です");
				return;
			}
			// 売上額に文字が入ったらエラー処理
			if (!rcdData.get(2).matches("^[0-9]*$")) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
			// rcdDataの1番目の要素(支店コード)が不正だったらエラーを出力し、終了

			if (branchMap.get(rcdData.get(0)) == null) {
				System.out.println(rcdFiles.get(i) + "の支店コードが不正です");
				return;
			}
			// rcdDataの2番目の要素(商品コード)が不正だったらエラーを出力し、終了
			if (commodityMap.get(rcdData.get(1)) == null) {
				System.out.println(rcdFiles.get(i) + "の商品コードが不正です");
				return;
			}

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
		PrintWriter pw = null;
		try {
			File branchOutFile = new File(args[0], "branch.out");
			BufferedWriter bw = new BufferedWriter(new FileWriter(branchOutFile));
			pw = new PrintWriter(bw);
			for (Entry<String, Long> s : branchEntries) {
				pw.println(s.getKey() + "," + branchMap.get(s.getKey()) + "," + Long.toString(s.getValue()));
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try{
				if (pw != null) {
				pw.close();
				}
			}catch(NullPointerException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}

		}

		// 4 ソートしてcommodity.outの出力
		List<Map.Entry<String, Long>> commodityEntries = new ArrayList<Map.Entry<String, Long>>(
				commodityEarningsMap.entrySet());
		Collections.sort(commodityEntries, new Comparator<Map.Entry<String, Long>>() {
			@Override
			public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
				return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
			}
		});
		try {
			File commodityOutFile = new File(args[0], "commodity.out");
			BufferedWriter cbw = new BufferedWriter(new FileWriter(commodityOutFile));
			pw = new PrintWriter(cbw);
			for (Entry<String, Long> s : commodityEntries) {
				pw.println(s.getKey() + "," + commodityMap.get(s.getKey()) + "," + Long.toString(s.getValue()));
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try{
				if (pw != null) {
					pw.close();
				}
			}catch(NullPointerException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
	}

}

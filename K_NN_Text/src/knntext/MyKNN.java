package knntext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;


public class MyKNN {
	private static String defaultPath = "E:\\java_Eclipse\\NBM2\\Reduced";
	private static File traningTextDir;// 存放待训练文本的目录
	private static String[] traningFileClassifications;//每篇文章的文件名
	private static SaveWord[] saveword;//存储所有训练文章的分词
	private static SaveWord[] _test_text;//存储所有的待分类文章的分词
	private static int _allnum;//记录一共有几篇训练文章
	private static int K;
	private static HashMap<String, Double> idf;
	private static HashSet<String> _allWordSet;
	//private static HashMap<String, Double> tempHashMap;//构造一个模板以便快速统计以训练集所有分词为维度构建的向量空间模型下各个训练文章的词频
	private static double right;//正确率
	
	public static void install() throws IOException
    {
		_allWordSet = new HashSet<String>();
		idf = new HashMap<String,Double>();
		K = 18;
		right = 0.0d;
        traningTextDir = new File(defaultPath);
        if (!traningTextDir.isDirectory())
        {
            throw new IllegalArgumentException("训练语料库搜索失败！ [" +defaultPath + "]");
        }
        traningFileClassifications = traningTextDir.list();//罗列出训练集的所有类别
        File tempFile;
        for(int i = 0; i < traningFileClassifications.length; i++){
        	tempFile = new File(defaultPath + File.separator + traningFileClassifications[i]);
        	_allnum = _allnum + tempFile.list().length;
        } 
    }
	
	public static void installword() throws IOException{//将每篇训练文章分词后存储到hashset中
		saveword = new SaveWord[_allnum];
		int index = 0;
		NlpirMethod.Nlpir_init();
		for (int i = 0; i < traningFileClassifications.length; i++) {
			File tempFile = new File(defaultPath + File.separator
					+ traningFileClassifications[i]);
			String[] ret = tempFile.list();
			for (int j = 0; j < ret.length; j++) {
				InputStreamReader isReader = new InputStreamReader(
						new FileInputStream(traningTextDir.getPath()
								+ File.separator
								+ traningFileClassifications[i]
								+ File.separator + ret[j]), "GBK");
				BufferedReader reader = new BufferedReader(isReader);
				String aline;
				StringBuilder sb = new StringBuilder();
				while ((aline = reader.readLine()) != null) {
					sb.append(aline + " ");
				}
				isReader.close();
				reader.close();
				String []temp = NlpirMethod.NLPIR_ParagraphProcess(sb.toString(), 0).split(" ");
				temp = DropStopWords(temp);
				
				saveword[index] = new SaveWord();
				saveword[index].classyString = traningFileClassifications[i];//记录当前文章属于哪个类别
				
				for(int k = 0; k < temp.length; k ++){
					if(saveword[index]._wordmap.containsKey(temp[k])){
						//saveword[index].sum_word_of_this_text++;
						double tempnum = saveword[index]._wordmap.get(temp[k]) + 1.0d;
						saveword[index]._wordmap.put(temp[k], tempnum);
					}
					else{
						//saveword[index].sum_word_of_this_text++;
						saveword[index]._wordmap.put(temp[k], 1.0d);
					}
						
				}
				for(int k = 0; k < temp.length; k++){
					double tempnum = saveword[index]._wordmap.get(temp[k])/temp.length;
					saveword[index]._wordmap.put(temp[k], tempnum);
				}
				_allWordSet.addAll(saveword[index]._wordmap.keySet());
				index++;
			}

		}//for循环结束saveword中存储了所有文章的词频,_allWordSet中存储了所有的训练文章的词
		
		/*Object[] tempStrings = _allWordSet.toArray();
		tempHashMap = new HashMap<String,Double>();
		for(int i= 0; i < tempStrings.length; i++){
			tempHashMap.put((String) tempStrings[i], 0.0d);
		}//构造一个模板以便快速统计以训练集所有分词为维度构建的向量空间模型下各个训练文章的词频	
		System.out.println("模板构造完毕");*/
		
		/*for(int i = 0; i < saveword.length; i++){
			HashMap<String, Double> tempsaveword = new HashMap<String,Double>(tempHashMap);
			Set set = saveword[i]._wordmap.keySet();
			Object[] tempset = set.toArray();
			for(int j = 0;j < tempset.length; j++){
				tempsaveword.put((String) tempset[j], saveword[i]._wordmap.get(tempset[j]));
			}
			System.out.println(tempsaveword.size());
			System.out.println(i);
			saveword[i]._wordmap.clear();
			saveword[i]._wordmap = null;
			saveword[i]._wordmap = new HashMap<String,Double>(tempsaveword);
			tempsaveword.clear();  
			tempsaveword=null; 
			System.gc();
		}//处理完后saveword存储了以训练集所有分词为维度构建的向量空间模型下各个训练文章的词频
		*/
		Object[]  _allWordSetStrings = _allWordSet.toArray();
		for(int i = 0; i < _allWordSetStrings.length; i++){
			double num = 0;
			for(int j = 0; j < saveword.length; j++){
				if(saveword[j]._wordmap.containsKey(_allWordSetStrings[i]))
					num++;
			}
			idf.put((String) _allWordSetStrings[i], Math.log(_allnum/num + 0.01));
		}
		
		
		
		
		
		
		System.out.println("训练结束");
	}
	
	public static void installTestText() throws IOException{//处理所有测试集
		NlpirMethod.Nlpir_init();
		int index = 0;
		File file = new File("E:\\java_Eclipse\\K_NN_Text\\测试集");
    	String []filepathStrings = file.list();
    	int test_text_num = 0;
    	for(int i = 0; i < filepathStrings.length; i++){
    		File file2 = new File("E:\\java_Eclipse\\K_NN_Text\\测试集\\" + filepathStrings[i]);
    		String []filepathStrings2 = file2.list();
    		test_text_num = test_text_num + filepathStrings2.length;
    	}//统计测试集有几篇文章
		_test_text = new SaveWord[test_text_num];
		
		System.out.println("测试集有" + test_text_num);
		
    	
    	for(int i = 0; i <filepathStrings.length; i++){
    		File file2 = new File("E:\\java_Eclipse\\K_NN_Text\\测试集\\" + filepathStrings[i]);
    		String []filepathStrings2 = file2.list();
    		for(int j = 0; j < filepathStrings2.length; j++){
    			_test_text[index] = new SaveWord();
    			_test_text[index].classyString = filepathStrings[i];////////////////////
    			InputStreamReader isReader =new InputStreamReader(new FileInputStream("E:\\java_Eclipse\\K_NN_Text\\测试集\\"+filepathStrings[i] + "\\" + filepathStrings2[j]),"GBK");
    			BufferedReader reader = new BufferedReader(isReader);
    			String aline;
    			StringBuilder sb = new StringBuilder();
    			while ((aline = reader.readLine()) != null)
    			{
    				sb.append(aline + " ");
    				}
    			isReader.close();
    			reader.close();
    			String sSrc = sb.toString();
    			String[] terms = NlpirMethod.NLPIR_ParagraphProcess(sSrc, 0).split(" ");//中文分词处理
    			terms = DropStopWords(terms);//去掉停用词，以免影响分词
    			for(int k = 0; k < terms.length; k ++){
					if(_test_text[index]._wordmap.containsKey(terms[k])){
						_test_text[index].sum_word_of_this_text++;
						double tempnum = _test_text[index]._wordmap.get(terms[k]) + 1.0d;
						_test_text[index]._wordmap.put(terms[k], tempnum);
					}
					else{
						_test_text[index].sum_word_of_this_text++;
						_test_text[index]._wordmap.put(terms[k], 1.0d);
					}
						
				}
    			for(int k = 0; k < terms.length; k++){
					double tempnum = _test_text[index]._wordmap.get(terms[k])/terms.length;
					_test_text[index]._wordmap.put(terms[k], tempnum);
				}
    			index++;
    		}
    	}//for循环结束后_test_text就存储了所有待分类文本的词频
    	System.out.println("测试集词频统计完毕");
    	
		/*for(int i = 0; i < _test_text.length; i++){
			HashMap<String, Double> tempsaveword = new HashMap<String,Double>(tempHashMap);
			Set set = _test_text[i]._wordmap.keySet();
			Object[] tempset = set.toArray();
			for(int j = 0;j < tempset.length; j++){
				tempsaveword.put((String) tempset[j], _test_text[i]._wordmap.get(tempset[j]));
			}
			_test_text[i]._wordmap = new HashMap<String,Double>(tempsaveword);
			tempsaveword.clear();  
			tempsaveword=null; 
		}*/
		System.out.println("测试集预处理结束");
	}
	
	public static String[] DropStopWords(String[] oldWords)//---------------------------------------去除停用词
    {
        Vector<String> v1 = new Vector<String>();
        for(int i=0;i<oldWords.length;++i)
        {
            if(StopWordsHandler.IsStopWord(oldWords[i])==false)
            {//不是停用词
                v1.add(oldWords[i]);
            }
        }
        String[] newWords = new String[v1.size()];//将vector集合类转化成字符串数组以便后续操作
        v1.toArray(newWords);
        return newWords;
    }
	
	@SuppressWarnings("unchecked")
	public static String[] knn(){
		Object[] temp_allWordSet = _allWordSet.toArray();
		String[] resultStrings = new String[ _test_text.length];//存储分类的结果
		
		for(int i = 0; i < _test_text.length; i++){
			Set settemp2 = _test_text[i]._wordmap.keySet();
			Object[] t2 = settemp2.toArray();
		//for(int i = 0; i < 1; i++){
			TreeMap<Double, String> ret = new TreeMap<Double,String>();
			for(int j = 0; j < saveword.length; j++){
			//for(int j = 0; j < 1; j++){
				double sum1 = 0.0d;//余弦相似度公式的分子
				double sum2 = 0.0d;//余弦相似度的分母第一个因子
				double sum3 = 0.0d;//余弦相似度的分母第二个因子
				//double A = 0.0d;
				//double B = 0.0d;
				
				Set settemp = saveword[j]._wordmap.keySet();
				Object[] t1 = settemp.toArray();
				for(int q = 0; q < t1.length; q++){
					sum2 = sum2 + (saveword[j]._wordmap.get(t1[q])*idf.get(t1[q]))*(saveword[j]._wordmap.get(t1[q])*idf.get(t1[q]));
					
				}
				//System.out.println("sum2 = " + sum2);
				for(int q = 0; q < t2.length; q++){
					if(_allWordSet.contains(t2[q])){
						sum3 = sum3 +( _test_text[i]._wordmap.get(t2[q])*idf.get(t2[q]))*(_test_text[i]._wordmap.get(t2[q])*idf.get(t2[q]));
						if(saveword[j]._wordmap.containsKey(t2[q]))
							sum1 = sum1 + (_test_text[i]._wordmap.get(t2[q])*idf.get(t2[q]))*(saveword[j]._wordmap.get(t2[q])*idf.get(t2[q]));
					}
					
				}
				//System.out.println("sum3 = " + sum3);
				//System.out.println("sum1 = " + sum1);
				
				
				
				
				/*for(int q = 0; q < temp_allWordSet.length; q++){
					if(_test_text[i]._wordmap.containsKey(temp_allWordSet[q]))
						A = _test_text[i]._wordmap.get(temp_allWordSet[q]);
					else 
						A = 0.0d;
					if(saveword[j]._wordmap.containsKey(temp_allWordSet[q]))
						B = saveword[j]._wordmap.get(temp_allWordSet[q]);
					else 
						B = 0.0d;
					
					//sum1 = sum1 + _test_text[i]._wordmap.get(temp_allWordSet[q])*saveword[j]._wordmap.get(temp_allWordSet[q]);
					//sum2 = sum2 + _test_text[i]._wordmap.get(temp_allWordSet[q])*_test_text[i]._wordmap.get(temp_allWordSet[q]);
					//sum3 = sum3 + saveword[j]._wordmap.get(temp_allWordSet[q])*saveword[j]._wordmap.get(temp_allWordSet[q]);
					sum1 = sum1 + A * B;
					sum2 = sum2 + A * A;
					sum3 = sum3 + B * B;
				}*/
				
				
				
				
				
				
				/*HashSet<String> set = new HashSet<String>(saveword[j]._wordmap.keySet());
				
				set.addAll(_test_text[i]._wordmap.keySet());//此时set中存储了待计算相似度的两篇文章的特征词
				
				Object[] wordkey = set.toArray();
				
				for(int  q = 0; q < wordkey.length; q++){
					int is = 0;
					if(_test_text[i]._wordmap.containsKey(wordkey[q]) && saveword[j]._wordmap.containsKey(wordkey[q]))
						is = 4;//该单词出现在了两篇文章里
					else if(_test_text[i]._wordmap.containsKey(wordkey[q]) && (!(saveword[j]._wordmap.containsKey(wordkey[q]))))
						is = 3;//该单词出现在了测试文章而没有出现在训练文章
					else if((!(_test_text[i]._wordmap.containsKey(wordkey[q]))) && saveword[j]._wordmap.containsKey(wordkey[q]))
						is = 2;//该单词出现在训练文章而没有出现在测试文章
					
					switch (is) {
					case 4:
						sum1 = sum1 + _test_text[i]._wordmap.get(wordkey[q])*saveword[j]._wordmap.get(wordkey[q]);
						sum2 = sum2 + _test_text[i]._wordmap.get(wordkey[q])*_test_text[i]._wordmap.get(wordkey[q]);
						sum3 = sum3 + saveword[j]._wordmap.get(wordkey[q])*saveword[j]._wordmap.get(wordkey[q]);
						break;	
					case 3:
						sum1 = sum1;
						sum2 = sum2 + _test_text[i]._wordmap.get(wordkey[q])*_test_text[i]._wordmap.get(wordkey[q]);
						sum3 = sum3 ;
						//System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
						break;
					case 2:
						sum1 = sum1;
						sum2 = sum2;
						sum3 = sum3 + saveword[j]._wordmap.get(wordkey[q])*saveword[j]._wordmap.get(wordkey[q]);
						//System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
						break;
					default:
						//System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
						break;
					}
					
					
				}*/
				//System.out.println(sum1/(Math.sqrt(sum2*sum3)));
				ret.put(sum1/(Math.sqrt(sum2*sum3)), saveword[j].classyString);
			}//for循环结束后ret中保存了当前待分类文章到每一个训练文章的距离并且按升序排序
			System.out.println(ret);////////////////////////////////////////////////////////
			
			Set<Double> set = ret.keySet();//取出ret中的键值以进行后续操作
			Object[] key_of_ret = set.toArray();
			//System.out.println(saveword.length);
			//System.out.println(key_of_ret.length);
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			for(int ii = key_of_ret.length-1; ii > key_of_ret.length-1-K; ii--){
				System.out.println(key_of_ret[ii] + "--" + ret.get(key_of_ret[ii]));
			}
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			String[] tempStrings = new String[K];
			
			for(int qq = 0; qq < K; qq++){
				tempStrings[qq] = ret.get(key_of_ret[key_of_ret.length-1-qq]);
			}//將前K個文章的类别取出来保存到tempStrings中
			System.out.println("============================================================begin");
			for(int qq = 0; qq < K; qq++){
				System.out.println(tempStrings[qq]);
			}
			System.out.println("===============================================================end");
			final HashMap<String, Double> tempHashMap = new HashMap<String,Double>();
			for(int qq = 0; qq < K; qq++){//统计最相似的k篇文章的所属类情况
				if(tempHashMap.containsKey(tempStrings[qq])){
					double temp = tempHashMap.get(tempStrings[qq]) + /*1.0d/*/((double)key_of_ret[key_of_ret.length-1-qq]*(double)key_of_ret[key_of_ret.length-1-qq]);
					tempHashMap.put(tempStrings[qq], temp);
				}
				else {
					tempHashMap.put(tempStrings[qq], /*1.0d/*/((double)key_of_ret[key_of_ret.length-1-qq]*(double)key_of_ret[key_of_ret.length-1-qq]));
				}
			}
			
			
			ArrayList keys = new ArrayList(tempHashMap.keySet());//得到key集合  
	        //把keys排序，但是呢，要按照后面这个比较的规则
	        java.util.Collections.sort(keys,new Comparator()//重载集合类的排序函数对所得分类条件概率进行排序以得到最大概率的类别
	        {
	            public int compare(final Object o1,final Object o2)
	            {
	                if(Double.parseDouble(tempHashMap.get(o1).toString())<Double.parseDouble(tempHashMap.get(o2).toString()))
	                    return 1;
	               
	                else if(Double.parseDouble(tempHashMap.get(o1).toString())==Double.parseDouble(tempHashMap.get(o2).toString()))
	                    return 0;
	              
	                else
	                    return -1;
	            }
	        });
	        for(int ii = 0; ii < keys.size(); ii++){
	        	System.out.println(tempHashMap.get(keys.get(ii)));
	        }
	        System.out.println( _test_text[i].classyString + "--" + (String)keys.get(0));
	        resultStrings[i] = _test_text[i].classyString + "--" + (String)keys.get(0);
	        if(_test_text[i].classyString.equals(keys.get(0)))
	        	right++;
		}
		return resultStrings;
	}
	
	public static void main(String[] args) throws IOException{
		install();
		System.out.println("exe----install");
		installword();
		System.out.println("exe----installword");
    	installTestText();
    	System.out.println("exe----installTestText");
    	System.out.println(saveword[0]._wordmap);
    	System.out.println(_test_text[0]._wordmap);
    	/*System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    	for(int i = 0; i < 30; i++){
    		System.out.println(saveword[i]._wordmap);
    	}
    	System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
    	for(int i = 0; i < 30; i++){
    		System.out.println(_test_text[i]._wordmap);
    	}
    	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    	*/
    	String[] ret = knn();
    	for(int i = 0; i < ret.length; i++){
    		System.out.println(ret[i]);
    	}
    	
    	System.out.println("正确率为：" + right/_test_text.length);
	}

}

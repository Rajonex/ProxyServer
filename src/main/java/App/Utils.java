package App;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static List<URL> readFromFileBlackList(String file)
    {
        List<URL> resultList = new ArrayList<URL>();
        if(Files.exists(Paths.get(file)))
        {
            try ( FileReader fileReader = new FileReader(file);
                  BufferedReader bufferedReader = new BufferedReader(fileReader);
            ){
                String nextLine = null;
               while((nextLine = bufferedReader.readLine()) != null)
               {
                   resultList.add(new URL(nextLine));
               }

            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return resultList;
    }

    public static Map<String, Statistics> openAndReadData (String fileName)
    {
        Map<String, Statistics> resultMap = new HashMap<>();

        File file = new File(fileName);
        if(file.exists())
        {
            try (FileReader fileReader = new FileReader(file);
                 BufferedReader bufferedReader = new BufferedReader(fileReader);)
            {

                String line;
                while((line = bufferedReader.readLine()) != null)
                {
                    String[] elements = line.split(",");
                    Statistics stat = new Statistics(Integer.parseInt(elements[1]), Long.parseLong(elements[2]), Long.parseLong(elements[3]));
                    resultMap.put(elements[0], stat);
                }
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return resultMap;
    }

    public static void saveData(Map<String, Statistics> dataMap, String fileName)
    {
        try (FileWriter fileWriter = new FileWriter(fileName, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter))
        {
            for(Map.Entry<String, Statistics> data : dataMap.entrySet())
            {
                bufferedWriter.write(data.getKey() + "," + data.getValue().getQuestionsNumber() + "," + data.getValue().getSendedData() + "," + data.getValue().getReceivedData() + "\n");
            }

        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}

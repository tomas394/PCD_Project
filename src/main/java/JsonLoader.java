import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class JsonLoader {
    public static List<Question> loadQuestions(String fileName) {
        Gson gson = new Gson();
        try {
            InputStream inputStream = JsonLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                System.err.println("Cannot find file: " + fileName);
                return null;
            }
            Reader reader = new InputStreamReader(inputStream);
            QuizFile quizFile = gson.fromJson(reader, QuizFile.class);
            if (quizFile != null) return quizFile.getQuestions();
        } catch (Exception e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
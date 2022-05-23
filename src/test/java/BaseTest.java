import com.kamennova.lala.Learner;
import com.kamennova.lala.Recognizer;
import com.kamennova.lala.persistence.Persistence;
import com.kamennova.lala.persistence.RedisPersistence;
import org.junit.BeforeClass;

public class BaseTest {
    protected Learner currLearn;
    protected Recognizer recognizer;
    protected Persistence persistence;

    protected Learner getLearnEntity(String pieceName) {
        if (this.currLearn == null) {
            this.currLearn = new Learner(pieceName, persistence);
        } else if (!this.currLearn.getPieceName().equals(pieceName)) {
            this.currLearn.finishLearn();
            this.currLearn.clear();
            this.currLearn.setNewPiece(pieceName);
        }

        return this.currLearn;
    }

   protected Recognizer getRecognizeEntity(boolean mightNeedNew) {
        if (this.recognizer == null) {
            System.out.println("create recognizer");
            this.recognizer = new Recognizer(persistence);
        }
//        else if (mightNeedNew) {
//            prevRecognizer = this.recognizer;
//            this.recognizer = new Recognizer(persistence);
//        }

       return recognizer;
    }

    @BeforeClass
    public void beforeClass() {
        this.persistence = new RedisPersistence();
    }

}

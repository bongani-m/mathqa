package dekauliya.fyp.mathqa.DataServices;

import android.graphics.Bitmap;
import android.net.Uri;

import com.orhanobut.logger.Logger;

import org.androidannotations.annotations.EBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import dekauliya.fyp.mathqa.Models.Concept;
import dekauliya.fyp.mathqa.Models.KeyPoint;
import dekauliya.fyp.mathqa.Models.Question;
import dekauliya.fyp.mathqa.Models.SearchResult;
import dekauliya.fyp.mathqa.Models.Solution;
import dekauliya.fyp.mathqa.Models.SubConcept;
import dekauliya.fyp.mathqa.Models.Topic;
import dekauliya.fyp.mathqa.Views.ListViews.Items.ConceptHeaderItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.ConceptSubItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.KeyPointHeaderItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.KeyPointSubItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.QuestionSubItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.SearchResultSubItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.SubConceptHeaderItem;
import dekauliya.fyp.mathqa.Views.ListViews.Items.TopicHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by dekauliya on 4/2/17.
 *
 * Provides the implementation of MathQA REST API client which requests or sends data from / to
 * MathQA server.
 */
@EBean(scope = EBean.Scope.Singleton)
public class DataService {
    private static MathQaRestApiInterface client = MathQaServiceGenerator.createService(MathQaRestApiInterface
            .class);

    List<AbstractFlexibleItem> mTopicConceptItems = new ArrayList<AbstractFlexibleItem>();
    List<AbstractFlexibleItem> mQuestionTopicItems = new ArrayList<AbstractFlexibleItem>();
    List<AbstractFlexibleItem> mConceptKeypointItems = new ArrayList<AbstractFlexibleItem>();
    List<AbstractFlexibleItem> mConceptFormulaItems = new ArrayList<AbstractFlexibleItem>();
    List<AbstractFlexibleItem> mQuestionConceptItems = new ArrayList<AbstractFlexibleItem>();
    List<AbstractFlexibleItem> mQuestionResultItems = new ArrayList<>();
    private Solution mSolution;

    HashMap<Integer, Topic> mTopics = new HashMap<>();
    HashMap<Integer, Concept> mConcepts = new HashMap<>();
    HashMap<Integer, SubConcept> mSubConcepts = new HashMap<>();

    /**
     * Gets list of topics and all concepts under each topic based on the chosen subject level.
     *
     * @param subjectId
     * @param mListener
     */
    public void getTopicConceptData(int subjectId, final IDataListener mListener){
        final DataType dataType = DataType.TOPIC_CONCEPT;
        clearItems(dataType);
        mTopics.clear();

        client.getTopics(subjectId)
                .flatMap(new Function<List<Topic>, Observable<Topic>>() {
                    @Override
                    public Observable<Topic> apply(List<Topic> topics) throws
                            Exception {
                        for(Topic topic: topics){
                            mTopics.put(topic.getId(), topic);
                        }
                        return Observable.fromIterable(topics);
                    }
                }).flatMap(new Function<Topic, ObservableSource<List<Concept>>>() {
                    @Override
                    public ObservableSource<List<Concept>> apply(Topic topic) throws Exception {
                        return client.getConcepts(topic.getId());
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Concept>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Concept> value) {
                        if (value != null && value.size() > 0){
                            Topic topic = mTopics.get(value.get(0).getTopic());
                            if (topic != null) {
                                TopicHeaderItem topicHeaderItem = new TopicHeaderItem(topic);
                                for(Concept concept: value){
                                    ConceptSubItem conceptSubItem = new ConceptSubItem
                                            (topicHeaderItem, concept);
                                    topicHeaderItem.addSubItem(conceptSubItem);
                                }
                                Collections.sort(topicHeaderItem.getSubItems(), mComparator);
                                addItem(topicHeaderItem, dataType, mComparator);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Gets all questions for the subject level and group them based on concepts which are available
     * under the chosen subject level.
     * @param subjectId
     * @param mListener
     */
    public void getQuestionTopicData(int subjectId, final IDataListener mListener){
        final DataType dataType = DataType.QUESTION_TOPIC;
        clearItems(dataType);
        mConcepts.clear();

        client.getTopics(subjectId)
            .flatMap(new Function<List<Topic>, Observable<Topic>>() {
                @Override
                public Observable<Topic> apply(List<Topic> topics) throws
                        Exception {
                    return Observable.fromIterable(topics);
                }
            }).flatMap(new Function<Topic, ObservableSource<List<Concept>>>() {
                @Override
                public ObservableSource<List<Concept>> apply(Topic topic) throws Exception {
                    return client.getConcepts(topic.getId());
                }
            }).flatMap(new Function<List<Concept>, ObservableSource<Concept>>() {
                @Override
                public ObservableSource<Concept> apply(List<Concept> concepts) throws
                        Exception {
                    for(Concept concept: concepts){
                        mConcepts.put(concept.getId(), concept);
                    }
                    return Observable.fromIterable(concepts);
                }
            }).flatMap(new Function<Concept, ObservableSource<List<Question>>>() {
                @Override
                public ObservableSource<List<Question>> apply(Concept concept) throws
                        Exception {
                    return client.getQuestions(concept.getId(), null);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<Question>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(List<Question> value) {
                    if (value != null && value.size() > 0){
                        Concept concept = mConcepts.get(value.get(0).getConcept());
                        if (concept != null) {
                            ConceptHeaderItem conceptHeaderItem = new ConceptHeaderItem
                                    (concept);
                            for(Question question: value){
                                QuestionSubItem questionSubItem = new QuestionSubItem
                                        (conceptHeaderItem, question);
                                conceptHeaderItem.addSubItem(questionSubItem);
                            }
                            Collections.sort(conceptHeaderItem.getSubItems(), mComparator);
                            addItem(conceptHeaderItem, dataType, mComparator);
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    mListener.onError();
                    Logger.e(e.getMessage());
                }

                @Override
                public void onComplete() {
                    mListener.onDataRetrieved();
                    Logger.d("LOADED ALL QUESTIONS BY TOPIC");
                }
            });
    }

    /**
     * Gets all the available concept keypoints under the chosen concept.
     * @param conceptId
     * @param mListener
     */
    public void getConceptKeypointData(int conceptId, final IDataListener mListener){
        final DataType dataType = DataType.KEYPOINT_CONCEPT;
        clearItems(dataType);

        client.getKeypointConcepts(conceptId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<KeyPoint>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<KeyPoint> value) {
                        if (value != null && value.size() > 0) {
                            for (KeyPoint keyPoint : value) {
                                KeyPointHeaderItem keyPointHeaderItem = new KeyPointHeaderItem
                                        (keyPoint);
                                KeyPointSubItem keyPointSubItem = new KeyPointSubItem(keyPointHeaderItem);
                                keyPointHeaderItem.addSubItem(keyPointSubItem);
                                addItem(keyPointHeaderItem, dataType, mComparator);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Gets all the available formula keypoints under the chosen concept.
     * @param conceptId
     * @param mListener
     */
    public void getConceptFormulaData(int conceptId, final IDataListener mListener){
        final DataType dataType = DataType.KEYPOINT_FORMULA;
        clearItems(dataType);

        client.getKeypointFormulas(conceptId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<KeyPoint>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<KeyPoint> value) {
                        if (value != null && value.size() > 0) {
                            for (KeyPoint keyPoint : value) {
                                KeyPointHeaderItem keyPointHeaderItem = new KeyPointHeaderItem
                                        (keyPoint);
                                KeyPointSubItem keyPointSubItem = new KeyPointSubItem(keyPointHeaderItem);
                                keyPointHeaderItem.addSubItem(keyPointSubItem);
                                addItem(keyPointHeaderItem, dataType, mComparator);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Get all questions available for the chosen concept and group them based on subconcepts which
     * are available under the chosen concept.
     * @param conceptid
     * @param mListener
     */
    public void getQuestionConceptData(int conceptid, final IDataListener mListener){
        final DataType dataType = DataType.QUESTION_CONCEPT;
        clearItems(dataType);
        mSubConcepts.clear();

        client.getSubConcepts(conceptid)
                .flatMap(new Function<List<SubConcept>, ObservableSource<SubConcept>>() {
                    @Override
                    public ObservableSource<SubConcept> apply(List<SubConcept> subConcepts) throws
                            Exception {
                        for(SubConcept subConcept: subConcepts){
                            mSubConcepts.put(subConcept.getId(), subConcept);
                        }
                        return Observable.fromIterable(subConcepts);
                    }
                }).flatMap(new Function<SubConcept, ObservableSource<List<Question>>>() {
                    @Override
                    public ObservableSource<List<Question>> apply(SubConcept subConcept) throws
                            Exception {
                        return client.getQuestions(null, subConcept.getId());
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Question>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Question> value) {
                        if (value != null && value.size() > 0){
                            SubConcept subconcept = mSubConcepts.get(value.get(0).getSubconcept());
                            if (subconcept != null) {
                                SubConceptHeaderItem subConceptHeaderItem = new
                                        SubConceptHeaderItem(subconcept);
                                for(Question question: value){
                                    QuestionSubItem questionSubItem = new QuestionSubItem
                                            (subConceptHeaderItem, question);
                                    subConceptHeaderItem.addSubItem(questionSubItem);
                                }
                                Collections.sort(subConceptHeaderItem.getSubItems(), mComparator);
                                addItem(subConceptHeaderItem, dataType, mComparator);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Gets the solution for the selected Question.
     * @param questionId
     * @param mListener
     */
    public void getSolution(String questionId, final IDataListener mListener){
        client.getSolutionsByQuestion(questionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Solution>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Solution> value) {
                        if (value.size() > 0) {
                            mSolution = value.get(0);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Sends a regular text query to requests for a database text search.
     * @param textQuery
     * @param mListener
     */
    public void searchTextDb(String textQuery, final IDataListener mListener){
        final DataType dataType = DataType.QUESTION_RESULT;
        clearItems(dataType);
        mQuestionResultItems.clear();
        Logger.d("TEXT QUERY: " + textQuery);
        client.searchDatabase(textQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<SearchResult> value) {
                        if (value != null && value.size() > 0) {
                            int index = 1;
                            for(SearchResult result: value){
                                SearchResultSubItem resultSubItem =
                                        new SearchResultSubItem(null, index++, false,
                                                null, result.getQuestion());
                                mQuestionResultItems.add(resultSubItem);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Sends a regular text query to requests for a full text search.
     * @param textQuery
     * @param mListener
     */
    public void searchFullText(String textQuery, final IDataListener mListener){
        final DataType dataType = DataType.QUESTION_RESULT;
        clearItems(dataType);
        mQuestionResultItems.clear();
        Logger.d("TEXT QUERY: " + textQuery);
        client.searchText(textQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<SearchResult> value) {
                        if (value != null && value.size() > 0) {
                            int index = 1;
                            for(SearchResult result: value){
                                SearchResultSubItem resultSubItem =
                                        new SearchResultSubItem(null, index++, false,
                                                null, result.getQuestion());
                                mQuestionResultItems.add(resultSubItem);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    /**
     * Sends a formula query in LaTeX to requests for a mathematical document search.
     * @param formulaQuery
     * @param mListener
     */
    public void searchFormula(String formulaQuery, final IDataListener mListener){
        final DataType  dataType = DataType.QUESTION_RESULT;
        clearItems(dataType);
        mQuestionResultItems.clear();
        Logger.d("FORMULA QUERY: " + formulaQuery);
        client.searchFormula(formulaQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<SearchResult> value) {
                        if (value != null && value.size() > 0){
                            int index = 1;
                            for(SearchResult fsr: value){
                                Logger.d("Related formula: " + fsr.getRel_formula().getContent());
                                if (fsr.getQuestion() != null) {
                                    Logger.d("Question: " + fsr.getQuestion().getContent());
                                }
                                SearchResultSubItem searchResultSubItem =
                                        new SearchResultSubItem(
                                                null, index++, true, fsr.getRel_formula(),
                                                fsr.getQuestion());
                                mQuestionResultItems.add(searchResultSubItem);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mListener.onError();
                        e.getStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        mListener.onDataRetrieved();
                    }
                });
    }

    public List<AbstractFlexibleItem> getData(DataType dataType){
        return new ArrayList<> (getDataByType(dataType));
    }

    public boolean isDataEmpty(DataType dataType){
        return getDataByType(dataType).isEmpty();
    }

    public void clearItems(DataType type){
        getDataByType(type).clear();
    }

    public void addItem(AbstractFlexibleItem item, DataType type, Comparator<AbstractFlexibleItem>
            mComparator) {
        List<AbstractFlexibleItem> mItems = getDataByType(type);
        mItems.add(item);
        Collections.sort(mItems, mComparator);
    }

    public Solution getSolution(){
        return this.mSolution;
    }

    public List<AbstractFlexibleItem> getDataByType(DataType dataType){
        switch(dataType){
            case TOPIC_CONCEPT: return mTopicConceptItems;
            case QUESTION_TOPIC: return mQuestionTopicItems;
            case KEYPOINT_CONCEPT: return mConceptKeypointItems;
            case KEYPOINT_FORMULA: return mConceptFormulaItems;
            case QUESTION_CONCEPT:  return mQuestionConceptItems;
            case QUESTION_RESULT: return mQuestionResultItems;
            default: return null;
        }
    }

    /**
     * All list items are sorted alphabetically with exception of question. Questions are sorted
     * according to their difficulty levels.
     */
    Comparator<AbstractFlexibleItem> mComparator = new Comparator<AbstractFlexibleItem>() {

        @Override
        public int compare(AbstractFlexibleItem o1, AbstractFlexibleItem o2) {
            if (o1 instanceof ConceptHeaderItem && o2 instanceof ConceptHeaderItem){
                return ((ConceptHeaderItem) o1).getConcept().getName()
                        .compareTo(((ConceptHeaderItem) o2).getConcept().getName());
            } else if (o1 instanceof ConceptSubItem && o2 instanceof ConceptSubItem){
                return ((ConceptSubItem) o1).getConcept().getName()
                        .compareTo(((ConceptSubItem) o2).getConcept().getName());
            } else if (o1 instanceof KeyPointHeaderItem && o2 instanceof KeyPointHeaderItem){
                return ((KeyPointHeaderItem) o1).getKeyPoint().getName()
                        .compareTo(((KeyPointHeaderItem) o2).getKeyPoint().getName());
            } else if (o1 instanceof QuestionSubItem && o2 instanceof QuestionSubItem) {
                int res = Integer.parseInt  (((QuestionSubItem) o1).getQuestion()
                        .getDifficulty_level()) - Integer.parseInt(((QuestionSubItem) o2)
                        .getQuestion().getDifficulty_level());
                return res != 0 ? res : ((QuestionSubItem) o1).getQuestion().getId()
                        .compareTo(((QuestionSubItem) o2).getQuestion().getId());
            } else if (o1 instanceof TopicHeaderItem && o2 instanceof TopicHeaderItem) {
                return ((TopicHeaderItem) o1).getTopic().getName()
                        .compareTo(((TopicHeaderItem) o2).getTopic().getName());
            } else if (o1 instanceof SubConceptHeaderItem && o2 instanceof SubConceptHeaderItem) {
                return ((SubConceptHeaderItem) o1).getSubConcept().getName()
                        .compareTo(((SubConceptHeaderItem) o2).getSubConcept().getName());
            }
            return 0;
        }
    };

    public void uploadImage(Uri uri){
        File file = new File(uri.getPath());

        MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_IMG, file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(),
                requestBody);

        client.uploadImage(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody value) {
                        Logger.d(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.d("Image uploaded!");
                    }
                });
    }


    public void uploadImage(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_IMG, byteArray);
        MultipartBody.Part body = MultipartBody.Part.create(requestBody);

        client.uploadImage(body)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody value) {
                Logger.d(value);
            }

            @Override
            public void onError(Throwable e) {
                Logger.e(e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.d("Image uploaded!");
            }
        });
    }

    public void postText(String text){
        client.postText(text)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody value) {
                Logger.d(value);
            }

            @Override
            public void onError(Throwable e) {
                Logger.e(e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.d("File successfully uploaded");
            }
        });
    }

    public static void updateClient(MathQaRestApiInterface newService){
        client = newService;
    }
}

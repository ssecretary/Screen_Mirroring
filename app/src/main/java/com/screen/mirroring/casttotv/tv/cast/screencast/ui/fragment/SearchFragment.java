package com.screen.mirroring.casttotv.tv.cast.screencast.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screen.mirroring.casttotv.tv.cast.screencast.retrofit.ApiService;
import com.screen.mirroring.casttotv.tv.cast.screencast.retrofit.RestApi;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.WebActivity;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.GoogleSearchAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements View.OnClickListener {

    public String url;
    int type_mode;
    WebActivity webActivity;

    private View view;
    private EditText search;
    private ImageView clear_search;
    private TextView cancel_search;
    private RecyclerView google_search;
    TextView txt_google_search;
    private LinearLayout no_search;
    ArrayList<String> googleSearchList;
    public static boolean isSearch = false;
    private GoogleSearchAdapter googleSearchAdapter;
    ApiService apiService;

    public SearchFragment(String s, int i, WebActivity webActivity) {
        this.url = s;
        type_mode = i;
        this.webActivity = webActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleSearchList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        apiService = RestApi.getClient().create(ApiService.class);
        initView();
        initListener();
        return view;
    }

    private void initListener() {
        clear_search.setOnClickListener(this);
        cancel_search.setOnClickListener(this);
    }

    private void initView() {
        search = (EditText) view.findViewById(R.id.search);
        clear_search = (ImageView) view.findViewById(R.id.clear_search);
        cancel_search = (TextView) view.findViewById(R.id.cancel_search);
        google_search = view.findViewById(R.id.google_search);
        txt_google_search = view.findViewById(R.id.txt_google_search);
        no_search = (LinearLayout) view.findViewById(R.id.no_search);

        search.setText(url);
        search.setSelection(search.getText().length());
        search.requestFocus();

        if (googleSearchList != null && googleSearchList.size() != 0) {
            google_search.setVisibility(View.VISIBLE);
            txt_google_search.setVisibility(View.VISIBLE);
        } else {
            google_search.setVisibility(View.GONE);
            txt_google_search.setVisibility(View.GONE);
        }

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        search.setOnEditorActionListener((v, actionId, event) -> {
            if (search.getText().toString().trim().length() > 0) {

                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                    webActivity.binding.webView.loadUrl("https://www.google.com/search?q=" + search.getText().toString().trim());
                    isSearch = true;
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    getFragmentManager().popBackStack();
                }
            } else {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                getFragmentManager().popBackStack();
            }
            return false;
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (search.getText().toString().length() == 0) {
                    googleSearchList.clear();
                    if (googleSearchList != null && googleSearchList.size() != 0) {
                        google_search.setVisibility(View.VISIBLE);
                        txt_google_search.setVisibility(View.VISIBLE);
                    } else {
                        google_search.setVisibility(View.GONE);
                        txt_google_search.setVisibility(View.GONE);
                    }

                } else {
                    Call<ResponseBody> call = apiService.getSearchUrl(search.getText().toString().trim());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            String str = null;
                            try {
                                str = response.body().string();
                                Log.e("search", "str" + str);
                                googleSearchList.clear();
                                Document doc = convertStringToXMLDocument(str);
                                doc.getDocumentElement().normalize();
                                Log.e("search", "Root element :" + doc.getDocumentElement().getNodeName());
                                NodeList nList = doc.getElementsByTagName("CompleteSuggestion");

                                for (int i = 0; i < nList.getLength(); i++) {
                                    // empList.add(getEmployee(nList.item(i)));
                                    //   if (nList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                    Element element = (Element) nList.item(i);
                                    Log.e("search", "element NodeName: " + element.getNodeName());


                                    //  Node node = (Node) nList.item(0);

                               /* NodeList nodeList = element.getElementsByTagName("CompleteSuggestion").item(0).getChildNodes();
                                Element item = (Element) nodeList.item(0);*/
                                    String searchString = getTagValue("suggestion", element);

                                    googleSearchList.add(searchString);
                                    Log.e("search", searchString);

                                }

                                if (googleSearchAdapter != null) {
                                    googleSearchAdapter.notifyDataSetChanged();
                                } else {
                                    googleSearchAdapter = new GoogleSearchAdapter(getActivity(), googleSearchList);
                                    google_search.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                                    google_search.setAdapter(googleSearchAdapter);

                                    googleSearchAdapter.setOnItemClickListener(new GoogleSearchAdapter.ClickListener() {
                                        @Override
                                        public void onItemClick(int position, View v, int type) {
                                            if (type == 1) {
                                                // search
                                                if (googleSearchList.get(position).length() > 0) {

                                                    webActivity.url = "https://www.google.com/search?q=" + googleSearchList.get(position).trim();
                                                    webActivity.loadWebView();
                                                    isSearch = true;
                                                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                                                    getFragmentManager().popBackStack();
                                                }
                                            } else {
                                                // string add
                                                search.setText(googleSearchList.get(position));
                                                search.setSelection(search.getText().length());

                                            }
                                        }
                                    });
                                }

                                if (googleSearchList != null && googleSearchList.size() != 0) {
                                    google_search.setVisibility(View.VISIBLE);
                                    txt_google_search.setVisibility(View.VISIBLE);
                                } else {
                                    google_search.setVisibility(View.GONE);
                                    txt_google_search.setVisibility(View.GONE);
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("Failure", "Message: " + t.getMessage());
                        }
                    });

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        //  NodeList list = element.getElementsByTagName(tag).item(0).getNodeName()
        Log.e("search", " element NodeName: " + element.getElementsByTagName(tag).item(0).getNodeName() +
                " LocalName " + element.getElementsByTagName(tag).item(0).getLocalName() +
                " NodeValue " + element.getElementsByTagName(tag).item(0).getNodeValue() +
                " TextContent " + element.getElementsByTagName(tag).item(0).getTextContent() +
                " Attribute Name  " + element.getElementsByTagName(tag).item(0).getAttributes().item(0).getNodeName() +
                " Attribute Value  " + element.getElementsByTagName(tag).item(0).getAttributes().item(0).getNodeValue()
        );
        // Node node = (Node) nodeList.item(0);
        return element.getElementsByTagName(tag).item(0).getAttributes().item(0).getNodeValue();
    }

    private static Document convertStringToXMLDocument(String xmlString) {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_search:
                getFragmentManager().popBackStack();
                break;
            case R.id.clear_search:
                search.setText("");
                break;
        }
    }
}

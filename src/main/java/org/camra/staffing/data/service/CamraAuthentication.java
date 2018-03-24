package org.camra.staffing.data.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.camra.staffing.util.CamraMember;
import org.camra.staffing.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CamraAuthentication {

    @Autowired private Properties properties;
    private XPath xpath;

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
    }


    public Optional<CamraMember> requestMemberDetails(int membership, String password) {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(properties.getBaseUrl());

        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("KEY", properties.getSecurityKey()));
        params.add(new BasicNameValuePair("memno", String.valueOf(membership)));
        params.add(new BasicNameValuePair("pass", password));

        httppost.setEntity(formEntity(params));

        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.commons.io.IOUtils.copy(entity.getContent(), baos);
            byte[] bytes = baos.toByteArray();

            InputSource in = new InputSource(new ByteArrayInputStream(bytes));

            Element root = (Element)xpath.evaluate("/xml", in, XPathConstants.NODE);
            String error = xpath.evaluate("Error", root);
            if (StringUtils.hasText(error)) {
                return Optional.empty();
            }

            return Optional.of(new CamraMember(
                    xpath.evaluate("Surname", root),
                    xpath.evaluate("Forename", root),
                    xpath.evaluate("Email", root),
                    xpath.evaluate("MembershipNumber", root)));


        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private UrlEncodedFormEntity formEntity(List<NameValuePair> parameters) {
        try {
            return new UrlEncodedFormEntity(parameters, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
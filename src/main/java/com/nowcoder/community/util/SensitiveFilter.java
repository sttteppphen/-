package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.Position;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private class TrieNode {


        private Map<Character, TrieNode> subNode = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode node) {
            subNode.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNode.get(c);
        }

        private boolean isSymble(Character c) {
            return !CharUtils.isAsciiAlphanumeric(c);
        }

        private TrieNode rootNode = new TrieNode();

        private boolean isKeywordEnd = false;


        @PostConstruct
        public void init() {
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
                String keyword;
                while ((keyword = reader.readLine()) != null) {
                    this.addKeyword(keyword);
                }
            } catch (IOException e) {

            }
        }

        private void addKeyword(String keyword) {
            TrieNode tempNode = rootNode;
            for (int i = 0; i < keyword.length(); i++) {
                char c = keyword.charAt(i);
                TrieNode subNode = tempNode.getSubNode(c);

                if (subNode == null) {
                    subNode = new TrieNode();
                    tempNode.addSubNode(c, subNode);
                }
                tempNode = subNode;

                if (i == keyword.length() - 1) {
                    tempNode.setKeywordEnd(true);
                }
            }
        }
    }
        public String filter(String text) {
            if (StringUtils.isBlank(text)) {
                return null;
            }

            TrieNode tempNode = rootNode;
            int begin = 0;
            int position = 0;
            StringBuilder sb = new StringBuilder();

            while (position < text.length()) {
                char c = text.charAt(position);
                if (isSymble(c)) {
                    if (tempNode == rootNode) {
                        sb.append(c);
                        begin++;
                    }
                    position++;
                    continue;
                }
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    tempNode = rootNode;
                } else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                } else {
                    position++;

                }
            }
            sb.append(text.substring(begin));

            return sb.toString();
        }



}

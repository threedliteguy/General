The unsupervised LDA algorithm produces a list of a specified number of topics from a text corpus. 
A topic is defined by a list of frequently co-occurring words.
The quality of the topics can be tuned by adjusting the number of topics and the number of words per topic.
Every run of LDA will produce a different topic grouping, numbering and assignment to pages.
The program lda.py was run on the text version of the Talmud in the Sefaria-Export github project.
The output files for the various runs list the top matching generated topic for each page, and other topics that may also be related by percentage match.  

Example from output-b-y-4.txt:
|J_Moed_Yoma.txt-005a                              |44   |44:62.8%, 6:29.9%, 73:0.0%  |david, king, samuel, joshua, kings, people, jewish, ii, lord, saul  

The text is :
Any generation in which the Beis Hamikdosh is not rebuilt is considered as if they had destroyed it.

The topic words do not appear in the text, but the words of the text presumably have a grouping with the topic words elsewhere in the corpus.  B_Kodashim_Arakhin.txt-032b was assigned this same topic by this run, and is more obviously related to the literal topic words.

What I like about LDA is that, while the tuning parameters are arbitrary, it does not bring in any pre-existing concepts from other training texts, as is done by GPT2 or word2vec.



package com.zaa.documentgen.gen.pdfMark;

import com.aspose.pdf.GoToAction;
import com.aspose.pdf.OutlineCollection;
import com.aspose.pdf.OutlineItemCollection;
import com.aspose.words.*;
import com.zaa.documentgen.model.PdfBookModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PdfMarkBuilder {

    /**swagger PDF插入标签**/
    public static void pdfAddMark(Document doc, File pdfFile)
    {
        List<PdfBookModel> allTitle =  PdfMarkBuilder.getAllTitle(doc);
        String absolutePath = pdfFile.getAbsolutePath();
        com.aspose.pdf.Document pdfDoc = new com.aspose.pdf.Document(pdfFile.getAbsolutePath());
        OutlineCollection outlineCollection = pdfDoc.getOutlines();
        for (PdfBookModel model : allTitle) {
            OutlineItemCollection outlineItem = new OutlineItemCollection(outlineCollection);
            outlineItem.setTitle(model.getTitle());
            outlineItem.setAction(new GoToAction(model.getPage()));
            outlineCollection.add(outlineItem);
            pdfAddChildMark( outlineCollection,outlineItem, model);
        }
        pdfDoc.save(absolutePath);

    }

    private static void pdfAddChildMark(OutlineCollection outlineCollection,OutlineItemCollection parent, PdfBookModel model)
    {
        List<PdfBookModel> children = model.getChild();
        if (children.size() > 0)
        {
            for (PdfBookModel subModel : children )
            {
                OutlineItemCollection outlineItem = new OutlineItemCollection(outlineCollection);
                outlineItem.setTitle(subModel.getTitle());
                outlineItem.setAction(new GoToAction(subModel.getPage()));
                parent.add(outlineItem);
                pdfAddChildMark(outlineCollection, outlineItem,subModel);
            }
        }
    }

    /**markdown to word ,and some word invert to one word, PDF插入标签**/
    public static void pdfAddMdMark(List<PdfBookModel> data , File pdfFile ) throws FileNotFoundException
    {
        String absolutePath = pdfFile.getAbsolutePath();
        com.aspose.pdf.Document pdfDoc = new com.aspose.pdf.Document(pdfFile.getAbsolutePath());

        OutlineCollection outlineCollection = pdfDoc.getOutlines();
        for (PdfBookModel model : data) {
            OutlineItemCollection outlineItem = new OutlineItemCollection(outlineCollection);
            outlineItem.setTitle(model.getTitle());
            outlineItem.setAction(new GoToAction(model.getPage()));
            outlineCollection.add(outlineItem);
            pdfAddMdChildMark( outlineCollection,outlineItem, model);
        }

        pdfDoc.save(absolutePath);

    }

    private static void pdfAddMdChildMark(OutlineCollection outlineCollection,OutlineItemCollection parent, PdfBookModel model)
    {
        List<PdfBookModel> children = model.getChild();
        if (children.size() > 0)
        {
            for (PdfBookModel subModel : children )
            {
                boolean equals = subModel.getTitle().equals("");
                if(equals){
                    pdfAddMdChildMark(outlineCollection, parent , subModel);
                }else{
                    OutlineItemCollection outlineItem = new OutlineItemCollection(outlineCollection);
                    outlineItem.setTitle(subModel.getTitle());
                    outlineItem.setAction(new GoToAction(subModel.getPage()));
                    parent.add(outlineItem);
                    pdfAddMdChildMark(outlineCollection, outlineItem , subModel);
                }
            }
        }
    }
    /**
     * 获得文档所有标题
     * @return
     */
    public static List<PdfBookModel> getAllTitle(Document originDoc){

        //用来存放所有标题，一级标题集合
        List<PdfBookModel> data = new ArrayList<>();
        try
        {

            // 后面需要用这个对象去获取当前段落所在的页码
            LayoutCollector layoutCollector = new LayoutCollector(originDoc);
            // 需要获取所有的section，要不然部分word提取目录不完整
            Section[] sections = originDoc.getSections().toArray();
            // 只获得正文的标题
            List<Paragraph> paragraphs = new ArrayList<>();
            for (int sec = 0; sec < sections.length; sec++){
                List<Paragraph> paragraph = Arrays.asList(sections[sec].getBody().getParagraphs().toArray());
                paragraphs.addAll(paragraph);
            }

            for (Paragraph p : paragraphs)
            {
                /** 层级 **/
                int level = p.getParagraphFormat().getOutlineLevel();
                if (level < 0 || level > 1) {
                    continue;
                }

                /** 页码*/
                int pageIndex = layoutCollector.getEndPageIndex(p);

                /** 标题内容 **/
                String text = "";
                for (Run run : p.getRuns())
                {
                    text += run.getText();
                }
                if (text == "")
                {
                    continue;
                }

                /** 装填标签Model **/
                PdfBookModel model = new PdfBookModel();
                model.setLevel(level)  ;
                model.setTitle( text ) ;
                model.setPage(pageIndex);

                /** 装填标签Model **/
                if (level == 0)
                {
                    data.add(model);
                }
                else
                {
                    getChildTitle(data.get(data.size() - 1), model);
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return data;
    }

    private static void getChildTitle(PdfBookModel parent, PdfBookModel child)
    {
        if (parent.getLevel() + 1 == child.getLevel())
        {
            parent.getChild().add(child);
        }
        else
        {
            getChildTitle(parent.getChild().get(parent.getChild().size() - 1) ,child);
        }
    }

}

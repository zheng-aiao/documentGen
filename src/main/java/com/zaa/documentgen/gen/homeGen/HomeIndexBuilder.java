package com.zaa.documentgen.gen.homeGen;

import com.aspose.words.*;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.gen.AsposeDocGenerator;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;

public class HomeIndexBuilder {

    /**
     * 生成封面(首页)
     *
     * @param instance
     * @param index
     * @param  builder
     */
    public static DocumentBuilder generateHomeIndex(DocxConf instance, Map<String, String> index, DocumentBuilder builder )
    {

        try{

            /** 0.设置段落字体格式 */
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setLineSpacing(18);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
            builder.getFont().setName(instance.getIndexFont());
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setBold(true);
            builder.getFont().setSize(instance.getIndexTitleFontSize());
            builder.writeln();
            /**1.插入图片和首页大标题**/
            InputStream genewImageInputStream = AsposeDocGenerator.class.getClassLoader().getResourceAsStream("genew.png");
            builder.insertImage(genewImageInputStream, RelativeHorizontalPosition.MARGIN, 0, RelativeVerticalPosition.MARGIN,
                    100, 70.0, 75.0, WrapType.TIGHT);
            genewImageInputStream.close();
            builder.write(instance.getIndexTitleName());
            /**2.插入首页副标题**/
            builder.writeln();
            builder.getFont().setSize(instance.getIndexSubTitleFontSize());
            builder.writeln(instance.getIndexSubTitleName() + instance.getName() +" 接口文档");
            builder.writeln();
            builder.writeln();
            /**3.插入描述信息**/
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setLineSpacing(24);
            builder.getParagraphFormat().setLeftIndent(70);
            builder.getFont().setSize(instance.getIndexDescFontSize());

            builder.writeln(instance.getDocxVersion().concat(index.get(DocxConf.HOME_VERSIONDOCX)));
            builder.writeln(instance.getContactEmail().concat(index.get(DocxConf.HOME_EMAIL)));
            builder.writeln(instance.getContactUrl ().concat(index.get(DocxConf.HOME_URL)));
            builder.writeln(instance.getDocxTime().concat(index.get(DocxConf.HOME_TIME)));


        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return builder;
    }
}

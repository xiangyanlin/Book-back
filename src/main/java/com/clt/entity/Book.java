package com.clt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * (Book)实体类
 *
 * @author makejava
 * @since 2020-02-25 19:03:24
 */
@Getter
@Setter
@ToString
@ApiModel("书籍实体")
public class Book implements Serializable {
    private static final long serialVersionUID = -35522362005481450L;
    /**
     * 书籍id
     */
    @ApiModelProperty("书籍id")
    private String bookId;
    /**
     * 类别id
     */
    @ApiModelProperty("类别id")
    private String categoryId;
    /**
     * 书籍名称
     */
    @ApiModelProperty("书籍名称")
    private String bookName;
    /**
     * 书籍描述
     */
    @ApiModelProperty("书籍描述")
    private String bookDescribe;
    /**
     * 书籍作者
     */
    @ApiModelProperty("书籍作者")
    private String author;
    /**
     * 出版社
     */
    @ApiModelProperty("出版社")
    private String published;
    /**
     * 价格
     */
    @ApiModelProperty("价格")
    private Double price;
    /**
     * 书籍状态  在库 已借 损坏
     */
    @ApiModelProperty("书籍状态  在库 已借 损坏")
    private String bookStatus;
    /**
     * 书籍封面
     */
    @ApiModelProperty("书籍封面")
    private String img;
    /**
     * 是否是电子书
     */
    @ApiModelProperty("是否是电子书")
    private Integer ebook;
    /**
     * 书籍位置
     */
    @ApiModelProperty("书籍位置")
    private String location;
    /**
     * 点赞数量
     */
    @ApiModelProperty("点赞数量")
    private Integer zanNumber;
    /**
     * 得分
     */
    @ApiModelProperty("得分")
    private Float score;
    /**
     * 录入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("录入时间")
    private Date inputTime;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("修改时间")
    private Date updateTime;

    /**
     * 借阅次数
     */
    @ApiModelProperty("借阅次数")
    private Integer borrowingNumber;

    @ApiModelProperty("总浏览量")
    private Integer totalBrowse;

    /**
     * 备用字段1
     */
    @ApiModelProperty("备用字段1")
    private String remark1;

    /**
     * 备用字段2
     */
    @ApiModelProperty("备用字段2")
    private String remark2;

    /**
     * 备用字段3
     */
    @ApiModelProperty("备用字段3")
    private String remark3;

    /**
     * 备用字段4
     */
    @ApiModelProperty("备用字段4")
    private String remark4;

    public void increaseZanNumber(){
        this.zanNumber++;
    }

    public void decreaseZanNumber(){
        this.zanNumber--;
    }

    public void increaseBorrowingNumber(){
        this.borrowingNumber++;
    }

    public void decreaseBorrowingNumber(){
        this.borrowingNumber--;
    }

    public void calculateScore(float score){
        if (this.score == null || this.score == 0){
            this.score = score;
        } else {
            this.score = (this.score + score) / 2;
        }
    }
}
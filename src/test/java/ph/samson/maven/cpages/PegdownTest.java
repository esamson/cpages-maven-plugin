/*
 * Copyright 2015 Edward Samson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ph.samson.maven.cpages;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AnchorLinkNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrikeNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not really testing anything here but my understading of how PegDown works.
 */
public class PegdownTest {

    private static final Logger log
            = LoggerFactory.getLogger(PegdownTest.class);

    @Test
    public void testAst() throws IOException {
        PegDownProcessor pegdown = new PegDownProcessor();
        byte[] bytes = Files.readAllBytes(new File(
                "src/test/resources/test.md").toPath());
        char[] markdownSource = new String(bytes, StandardCharsets.UTF_8)
                .toCharArray();
        RootNode root = pegdown.parseMarkdown(markdownSource);
        root.accept(new TestVisitor());
        String html = pegdown.markdownToHtml(markdownSource);
        log.info("html: {}", html);
    }

    private static class TestVisitor implements Visitor {

        @Override
        public void visit(AbbreviationNode node) {
            log.debug(indent[indentLevel] + "AbbreviationNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(AnchorLinkNode node) {
            log.debug(indent[indentLevel] + "AnchorLinkNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(AutoLinkNode node) {
            log.debug(indent[indentLevel] + "AutoLinkNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(BlockQuoteNode node) {
            log.debug(indent[indentLevel] + "BlockQuoteNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(BulletListNode node) {
            log.debug(indent[indentLevel] + "BulletListNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(CodeNode node) {
            log.debug(indent[indentLevel] + "CodeNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(DefinitionListNode node) {
            log.debug(indent[indentLevel] + "DefinitionListNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(DefinitionNode node) {
            log.debug(indent[indentLevel] + "DefinitionNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(DefinitionTermNode node) {
            log.debug(indent[indentLevel] + "DefinitionTermNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(ExpImageNode node) {
            log.debug(indent[indentLevel] + "ExpImageNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(ExpLinkNode node) {
            log.debug(indent[indentLevel] + "ExpLinkNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(HeaderNode node) {
            log.debug(indent[indentLevel] + "HeaderNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(HtmlBlockNode node) {
            log.debug(indent[indentLevel] + "HtmlBlockNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(InlineHtmlNode node) {
            log.debug(indent[indentLevel] + "InlineHtmlNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(ListItemNode node) {
            log.debug(indent[indentLevel] + "ListItemNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(MailLinkNode node) {
            log.debug(indent[indentLevel] + "MailLinkNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(OrderedListNode node) {
            log.debug(indent[indentLevel] + "OrderedListNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(ParaNode node) {
            log.debug(indent[indentLevel] + "ParaNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(QuotedNode node) {
            log.debug(indent[indentLevel] + "QuotedNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(ReferenceNode node) {
            log.debug(indent[indentLevel] + "ReferenceNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(RefImageNode node) {
            log.debug(indent[indentLevel] + "RefImageNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(RefLinkNode node) {
            log.debug(indent[indentLevel] + "RefLinkNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(RootNode node) {
            log
                    .debug(indent[indentLevel] + indent[indentLevel]
                            + "RootNode: {}", node);
            visitChildren(node);
//            for (ReferenceNode reference : node.getReferences()) {
//                visitChildren(reference);
//            }
            visitChildren(node);
        }

        @Override
        public void visit(SimpleNode node) {
            log.debug(indent[indentLevel] + "SimpleNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(SpecialTextNode node) {
            log.debug(indent[indentLevel] + "SpecialTextNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(StrikeNode node) {
            log.debug(indent[indentLevel] + "StrikeNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(StrongEmphSuperNode node) {
            log.debug(indent[indentLevel] + "StrongEmphSuperNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableBodyNode node) {
            log.debug(indent[indentLevel] + "TableBodyNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableCaptionNode node) {
            log.debug(indent[indentLevel] + "TableCaptionNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableCellNode node) {
            log.debug(indent[indentLevel] + "TableCellNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableColumnNode node) {
            log.debug(indent[indentLevel] + "TableColumnNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableHeaderNode node) {
            log.debug(indent[indentLevel] + "TableHeaderNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableNode node) {
            log.debug(indent[indentLevel] + "TableNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TableRowNode node) {
            log.debug(indent[indentLevel] + "TableRowNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(VerbatimNode node) {
            log.debug(indent[indentLevel] + "VerbatimNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(WikiLinkNode node) {
            log.debug(indent[indentLevel] + "WikiLinkNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(TextNode node) {
            log.debug(indent[indentLevel] + "TextNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(SuperNode node) {
            log.debug(indent[indentLevel] + "SuperNode: {}", node);
            visitChildren(node);
        }

        @Override
        public void visit(Node node) {
            log.debug(indent[indentLevel] + "Node: {}", node);
        }

        private void visitChildren(SuperNode node) {
            indentLevel += 1;
            for (Node child : node.getChildren()) {
                child.accept(this);
            }
            indentLevel -= 1;
        }

        private void visitChildren(SimpleNode node) {
            indentLevel += 1;
            for (Node child : node.getChildren()) {
                child.accept(this);
            }
            indentLevel -= 1;
        }

        private void visitChildren(TextNode node) {
            indentLevel += 1;
            for (Node child : node.getChildren()) {
                child.accept(this);
            }
            indentLevel -= 1;
        }

        private int indentLevel = 0;
        private static final int MAX_INDENTS = 20;
        private static final String[] indent = new String[MAX_INDENTS];

        static {
            for (int i = 0; i < MAX_INDENTS; i++) {
                indent[i] = "";
                for (int j = 0; j < i; j++) {
                    indent[i] += "  ";
                }
            }
        }
    }
}

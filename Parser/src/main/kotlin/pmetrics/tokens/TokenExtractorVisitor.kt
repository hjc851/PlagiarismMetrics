//package token
//
//import com.github.javaparser.ast.*
//import com.github.javaparser.ast.body.*
//import com.github.javaparser.ast.comments.BlockComment
//import com.github.javaparser.ast.comments.JavadocComment
//import com.github.javaparser.ast.comments.LineComment
//import com.github.javaparser.ast.expr.*
//import com.github.javaparser.ast.modules.*
//import com.github.javaparser.ast.stmt.*
//import com.github.javaparser.ast.type.*
//import com.github.javaparser.ast.visitor.VoidVisitor
//
//class TokenExtractorVisitor: VoidVisitor<MutableList<Token>> {
//    override fun visit(n: NodeList<*>, arg: MutableList<Token>) {
//        n.forEach { it.accept(this, arg) }
//    }
//
//    override fun visit(n: AnnotationDeclaration, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: AnnotationMemberDeclaration, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

//    override fun visit(n: AssertStmt, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }


//    override fun visit(n: BinaryExpr, arg: MutableList<Token>) {
//        n.left.accept(this, arg)
//
//        when (n.operator) {
//            BinaryExpr.Operator.OR                      -> arg.add(Token(TokenType.OR, "||"))
//            BinaryExpr.Operator.AND                     -> arg.add(Token(TokenType.AND, "&&"))
//            BinaryExpr.Operator.BINARY_OR               -> arg.add(Token(TokenType.BINARY_OR, "|"))
//            BinaryExpr.Operator.BINARY_AND              -> arg.add(Token(TokenType.BINARY_AND, "&"))
//            BinaryExpr.Operator.XOR                     -> arg.add(Token(TokenType.XOR, "^"))
//            BinaryExpr.Operator.EQUALS                  -> arg.add(Token(TokenType.EQUALS, "=="))
//            BinaryExpr.Operator.NOT_EQUALS              -> arg.add(Token(TokenType.NOT_EQUALS, "!="))
//            BinaryExpr.Operator.LESS                    -> arg.add(Token(TokenType.LESS, "<"))
//            BinaryExpr.Operator.GREATER                 -> arg.add(Token(TokenType.GREATER, ">"))
//            BinaryExpr.Operator.LESS_EQUALS             -> arg.add(Token(TokenType.LESS_EQUALS, "<="))
//            BinaryExpr.Operator.GREATER_EQUALS          -> arg.add(Token(TokenType.GREATER_EQUALS, ">="))
//            BinaryExpr.Operator.LEFT_SHIFT              -> arg.add(Token(TokenType.LEFT_SHIFT, "<<"))
//            BinaryExpr.Operator.SIGNED_RIGHT_SHIFT      -> arg.add(Token(TokenType.SIGNED_RIGHT_SHIFT, ">>"))
//            BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT    -> arg.add(Token(TokenType.UNSIGNED_RIGHT_SHIFT, ">>>"))
//            BinaryExpr.Operator.PLUS                    -> arg.add(Token(TokenType.PLUS, "+"))
//            BinaryExpr.Operator.MINUS                   -> arg.add(Token(TokenType.MINUS, "-"))
//            BinaryExpr.Operator.TIMES                -> arg.add(Token(TokenType.TIMES, "*"))
//            BinaryExpr.Operator.DIVIDE                  -> arg.add(Token(TokenType.DIVIDE, "/"))
//            BinaryExpr.Operator.REMAINDER               -> arg.add(Token(TokenType.REMAINDER, "%"))
//
//            else -> throw IllegalStateException("Unknown binary operator ${n.operator}")
//        }
//
//        n.right.accept(this, arg)
//    }


//    override fun visit(n: CatchClause, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.CATCH, "catch"))
//        n.parameter.accept(this, arg)
//        arg.add(Token(TokenType.CATCHBODY_START, "{"))
//        n.body.statements.accept(this, arg)
//        arg.add(Token(TokenType.CATCHBODY_END, "}"))
//    }
//
//    override fun visit(n: CharLiteralExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NUMBER, n.value))
//    }
//
//    override fun visit(n: ClassExpr, arg: MutableList<Token>) {
//        n.type.accept(this, arg)
//        arg.add(Token(TokenType.CLASS, "class"))
//    }
//
//    override fun visit(n: ClassOrInterfaceDeclaration, arg: MutableList<Token>) {
//        n.annotations.accept(this, arg)
//        n.modifiers.accept(this, arg)
//
//        if (n.isInterface)
//            arg.add(Token(TokenType.INTERFACE, "interface"))
//        else
//            arg.add(Token(TokenType.CLASS, "class"))
//
//        n.name.accept(this, arg)
//
//        if (n.typeParameters.isNotEmpty()) {
//            arg.add(Token(TokenType.GENERICS_START, "<"))
//            n.typeParameters.accept(this, arg)
//            arg.add(Token(TokenType.GENERICS_END, ">"))
//        }
//
//        if (n.extendedTypes.isNotEmpty()) {
//            arg.add(Token(TokenType.EXTENDS, "extends"))
//            n.extendedTypes.accept(this, arg)
//        }
//
//        if (n.implementedTypes.isNotEmpty()) {
//            arg.add(Token(TokenType.IMPLEMENTS, "implements"))
//            n.implementedTypes.accept(this, arg)
//        }
//
//        arg.add(Token(TokenType.TYPEBODY_START, "{"))
//        n.members.accept(this, arg)
//        arg.add(Token(TokenType.TYPEBODY_END, "}"))
//    }
//
//    override fun visit(n: ClassOrInterfaceType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: CompilationUnit, arg: MutableList<Token>) {
//        n.packageDeclaration.ifPresent { it.accept(this, arg) }
//        n.imports.accept(this, arg)
//        n.types.accept(this, arg)
//    }
//
//    override fun visit(n: ConditionalExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.IF, "if"))
//        n.condition.accept(this, arg)
//        arg.add(Token(TokenType.CONDBODY_START, "{"))
//        n.thenExpr.accept(this, arg)
//        arg.add(Token(TokenType.CONDBODY_END, "}"))
//        arg.add(Token(TokenType.ELSE, "else"))
//        arg.add(Token(TokenType.CONDBODY_START, "{"))
//        n.elseExpr.accept(this, arg)
//        arg.add(Token(TokenType.CONDBODY_END, "}"))
//    }
//
//    override fun visit(n: ConstructorDeclaration, arg: MutableList<Token>) {
//        n.annotations.accept(this, arg)
//        arg.add(Token(TokenType.CONSTRUCTOR, ""))
//
//        if (n.typeParameters.isNotEmpty()) {
//            arg.add(Token(TokenType.GENERICS_START, "<"))
//            n.typeParameters.accept(this, arg)
//            arg.add(Token(TokenType.GENERICS_END, ">"))
//        }
//
//        n.name.accept(this, arg)
//
//        arg.add(Token(TokenType.PARAMETERS_START, "("))
//        n.parameters.accept(this, arg)
//        arg.add(Token(TokenType.PARAMETERS_END, ")"))
//
//        if (n.thrownExceptions.isNotEmpty()) {
//            arg.add(Token(TokenType.THROWS, "throws"))
//            arg.add(Token(TokenType.THROWS_START, ""))
//            n.thrownExceptions.accept(this, arg)
//            arg.add(Token(TokenType.THROWS_END, ""))
//        }
//
//        arg.add(Token(TokenType.CONSTRUCTOR_START, "{"))
//        n.body.statements.accept(this, arg)
//        arg.add(Token(TokenType.CONSTRUCTOR_END, "}"))
//    }
//
//    override fun visit(n: ContinueStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.CONTINUE, "continue"))
//        n.label.ifPresent { it.accept(this, arg) }
//    }
//
//    override fun visit(n: DoStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.DOWHILE, "do"))
//
//        arg.add(Token(TokenType.LOOPBODY_START, "{"))
//        if (n.body.isBlockStmt)
//            n.body.asBlockStmt().statements.accept(this, arg)
//        else
//            n.body.accept(this, arg)
//        arg.add(Token(TokenType.LOOPBODY_END, "}"))
//
//        n.condition.accept(this, arg)
//    }
//
//    override fun visit(n: DoubleLiteralExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NUMBER, n.value))
//    }
//
//    override fun visit(n: EmptyStmt, arg: MutableList<Token>) {}
//
//    override fun visit(n: EnclosedExpr, arg: MutableList<Token>) {
//        n.inner.accept(this, arg)
//    }
//
//    override fun visit(n: EnumConstantDeclaration, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: EnumDeclaration, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: ExplicitConstructorInvocationStmt, arg: MutableList<Token>) {
//        n.expression.ifPresent { it.accept(this, arg) }
//
//        if (n.isThis)
//            arg.add(Token(TokenType.NAME, "this"))
//        else
//            arg.add(Token(TokenType.NAME, "super"))
//
//        arg.add(Token(TokenType.ARGUMENTS_START, ""))
//        n.arguments.accept(this, arg)
//        arg.add(Token(TokenType.ARGUMENTS_END, ""))
//    }
//
//    override fun visit(n: ExpressionStmt, arg: MutableList<Token>) {
//        n.expression.accept(this, arg)
//    }
//
//    override fun visit(n: FieldAccessExpr, arg: MutableList<Token>) {
//        n.scope.accept(this, arg)
//        arg.add(Token(TokenType.DOT, "."))
//        n.name.accept(this, arg)
//    }
//
//    override fun visit(n: FieldDeclaration, arg: MutableList<Token>) {
//        n.variables.forEach { variable ->
//            n.annotations.accept(this, arg)
//            arg.add(Token(TokenType.FIELD, ""))
//            n.modifiers.accept(this, arg)
//            variable.type.accept(this, arg)
//            variable.name.accept(this, arg)
//
//            variable.initializer.ifPresent { expr ->
//                arg.add(Token(TokenType.ASSIGNMENT, "="))
//                expr.accept(this, arg)
//            }
//        }
//    }
//
//    override fun visit(n: ForStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.FOR, "for"))
//        n.initialization.accept(this, arg)
//        n.compare.ifPresent { compare -> compare.accept(this, arg) }
//        n.update.accept(this, arg)
//
//        arg.add(Token(TokenType.LOOPBODY_START, "{"))
//        if (n.body.isBlockStmt)
//            n.body.asBlockStmt().statements.accept(this, arg)
//        else
//            n.body.accept(this, arg)
//        arg.add(Token(TokenType.LOOPBODY_END, "}"))
//    }
//
//    override fun visit(n: ForEachStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.FOREACH, "foreach"))
//        n.variableDeclarator.accept(this, arg)
//        arg.add(Token(TokenType.COLON, ":"))
//        n.iterable.accept(this, arg)
//
//        arg.add(Token(TokenType.LOOPBODY_START, "{"))
//        if (n.body.isBlockStmt)
//            n.body.asBlockStmt().statements.accept(this, arg)
//        else
//            n.body.accept(this, arg)
//        arg.add(Token(TokenType.LOOPBODY_END, "}"))
//    }
//
//    override fun visit(n: IfStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.IF, "if"))
//        n.condition.accept(this, arg)
//
//        arg.add(Token(TokenType.CONDBODY_START, "{"))
//        if (n.thenStmt.isBlockStmt)
//            n.thenStmt.asBlockStmt().statements.accept(this, arg)
//        else
//            n.thenStmt.accept(this, arg)
//        arg.add(Token(TokenType.CONDBODY_END, "}"))
//
//        n.elseStmt.ifPresent { elseBranch ->
//            arg.add(Token(TokenType.ELSE, "else"))
//            elseBranch.accept(this, arg)
//        }
//    }
//
//    override fun visit(n: ImportDeclaration, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.IMPORT, "import"))
//        if (n.isStatic) arg.add(Token(TokenType.STATIC, "static"))
//        n.name.accept(this, arg)
//        if (n.isAsterisk) arg.add(Token(TokenType.ASTERIX, "*"))
//    }
//
//    override fun visit(n: InitializerDeclaration, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: InstanceOfExpr, arg: MutableList<Token>) {
//        n.expression.accept(this, arg)
//        arg.add(Token(TokenType.INSTANCEOF, "instanceof"))
//        n.type.accept(this, arg)
//    }
//
//    override fun visit(n: IntegerLiteralExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NUMBER, n.value))
//    }
//
//    override fun visit(n: IntersectionType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: JavadocComment, arg: MutableList<Token>) {}
//
//    override fun visit(n: LabeledStmt, arg: MutableList<Token>) {
//        n.label.accept(this, arg)
//        arg.add(Token(TokenType.COLON, ":"))
//        n.statement.accept(this, arg)
//    }
//
//    override fun visit(n: LambdaExpr, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: LineComment, arg: MutableList<Token>) {}
//
//    override fun visit(n: LocalClassDeclarationStmt, arg: MutableList<Token>) {
//        n.classDeclaration.accept(this, arg)
//    }
//
//    override fun visit(n: LongLiteralExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NUMBER, n.value))
//    }
//
//    override fun visit(n: MarkerAnnotationExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.ANNOTATIONMARKER, "@"))
//        n.name.accept(this, arg)
//    }
//
//    override fun visit(n: MemberValuePair, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: MethodCallExpr, arg: MutableList<Token>) {
//        n.scope.ifPresent { scope ->
//            scope.accept(this, arg)
//            arg.add(Token(TokenType.DOT, "."))
//        }
//
//        n.name.accept(this, arg)
//
//        n.typeArguments.ifPresent { typeArguments ->
//            arg.add(Token(TokenType.GENERICS_START, "<"))
//            typeArguments.accept(this, arg)
//            arg.add(Token(TokenType.GENERICS_END, ">"))
//        }
//
//        arg.add(Token(TokenType.ARGUMENTS_START, "("))
//        n.arguments.accept(this, arg)
//        arg.add(Token(TokenType.ARGUMENTS_END, ")"))
//    }
//
//    override fun visit(n: MethodDeclaration, arg: MutableList<Token>) {
//        n.annotations.accept(this, arg)
//        arg.add(Token(TokenType.METHOD, ""))
//        n.modifiers.accept(this, arg)
//
//        if (n.typeParameters.isNotEmpty()) {
//            arg.add(Token(TokenType.GENERICS_START, "<"))
//            n.typeParameters.accept(this, arg)
//            arg.add(Token(TokenType.GENERICS_END, ">"))
//        }
//
//        n.type.accept(this, arg)
//        n.name.accept(this, arg)
//
//        arg.add(Token(TokenType.PARAMETERS_START, "("))
//        n.parameters.accept(this, arg)
//        arg.add(Token(TokenType.PARAMETERS_END, ")"))
//
//        if (n.thrownExceptions.isNotEmpty()) {
//            arg.add(Token(TokenType.THROWS, "throws"))
//            arg.add(Token(TokenType.THROWS_START, ""))
//            n.thrownExceptions.accept(this, arg)
//            arg.add(Token(TokenType.THROWS_END, ""))
//        }
//
//        n.body.ifPresent { body ->
//            arg.add(Token(TokenType.METHOD_START, "{"))
//            body.statements.accept(this, arg)
//            arg.add(Token(TokenType.METHOD_END, "}"))
//        }
//    }
//
//    override fun visit(n: MethodReferenceExpr, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: NameExpr, arg: MutableList<Token>) {
//        n.name.accept(this, arg)
//    }
//
//    override fun visit(n: Name, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: NormalAnnotationExpr, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: NullLiteralExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NULL, "null"))
//    }
//
//    override fun visit(n: ObjectCreationExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NEW, "new"))
//
//        n.scope.ifPresent { scope ->
//            scope.accept(this, arg)
//            arg.add(Token(TokenType.DOT, "."))
//        }
//
//        n.type.accept(this, arg)
//
//        n.typeArguments.ifPresent { typeArguments ->
//            arg.add(Token(TokenType.GENERICS_START, "<"))
//            typeArguments.accept(this, arg)
//            arg.add(Token(TokenType.GENERICS_END, ">"))
//        }
//
//        arg.add(Token(TokenType.ARGUMENTS_START, "("))
//        n.arguments.accept(this, arg)
//        arg.add(Token(TokenType.ARGUMENTS_END, ")"))
//
//        n.anonymousClassBody.ifPresent { acb ->
//            arg.add(Token(TokenType.TYPEBODY_START, "{"))
//            acb.accept(this, arg)
//            arg.add(Token(TokenType.TYPEBODY_END, "}"))
//        }
//    }
//
//    override fun visit(n: PackageDeclaration, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.PACKAGE, "package"))
//        n.name.accept(this, arg)
//    }
//
//    override fun visit(n: Parameter, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.PARAMETER, ""))
//        n.annotations.accept(this, arg)
//        n.type.accept(this, arg)
//        if (n.isVarArgs) arg.add(Token(TokenType.VARARG, "..."))
//        n.name.accept(this, arg)
//    }
//
//    override fun visit(n: PrimitiveType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.type.asString()))
//    }
//
//    override fun visit(n: ReturnStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.RETURN, "return"))
//        n.expression.ifPresent { it.accept(this, arg) }
//    }
//
//    override fun visit(n: SimpleName, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: SingleMemberAnnotationExpr, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: StringLiteralExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.STRING, n.asString()))
//    }
//
//    override fun visit(n: SuperExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, "super"))
//    }
//
//    override fun visit(n: SwitchEntry, arg: MutableList<Token>) {
//        TODO()
//    }
//
//    override fun visit(n: SwitchStmt, arg: MutableList<Token>) {
//        n.entries.forEach { entry ->
//            n.selector.accept(this, arg)
//            arg.add(Token(TokenType.EQUALS, "="))
//
//            for (i in 0 until entry.labels.size) {
//                entry.labels[i].accept(this, arg)
//
//                if (i < entry.labels.size - 1)
//                    arg.add(Token(TokenType.AND, "&&"))
//            }
//
//            arg.add(Token(TokenType.CONDBODY_START, "{"))
//            entry.statements.accept(this, arg)
//            arg.add(Token(TokenType.CONDBODY_END, "}"))
//        }
//    }
//
//    override fun visit(n: SynchronizedStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.SYNCHRONIZED, "synchronized"))
//        n.expression.accept(this, arg)
//        n.body.accept(this, arg)
//    }
//
//    override fun visit(n: ThisExpr, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, "this"))
//    }
//
//    override fun visit(n: ThrowStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.THROW, "throw"))
//        n.expression.accept(this, arg)
//    }
//
//    override fun visit(n: TryStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.TRY, "try"))
//
//        arg.add(Token(TokenType.TRYRESOURCE_START, "("))
//        n.resources.accept(this, arg)
//        arg.add(Token(TokenType.TRYRESOURCE_END, ")"))
//
//        arg.add(Token(TokenType.TRYBODY_START, "{"))
//        n.tryBlock.statements.accept(this, arg)
//        arg.add(Token(TokenType.TRYBODY_END, "}"))
//
//        n.catchClauses.accept(this, arg)
//        n.finallyBlock.ifPresent {
//            arg.add(Token(TokenType.FINALLY, "finally"))
//            arg.add(Token(TokenType.CATCHBODY_START, "{"))
//            it.statements.accept(this, arg)
//            arg.add(Token(TokenType.CATCHBODY_END, "}"))
//        }
//    }
//
//    override fun visit(n: TypeExpr, arg: MutableList<Token>) {
//        n.type.accept(this, arg)
//    }
//
//    override fun visit(n: TypeParameter, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: UnaryExpr, arg: MutableList<Token>) {
//
//        if (n.isPostfix)
//            n.expression.accept(this, arg)
//
//        when (n.operator) {
//            UnaryExpr.Operator.PLUS -> arg.add(Token(TokenType.PLUS, "+"))
//            UnaryExpr.Operator.MINUS -> arg.add(Token(TokenType.MINUS, "-"))
//            UnaryExpr.Operator.PREFIX_INCREMENT -> arg.add(Token(TokenType.PLUS, "++"))
//            UnaryExpr.Operator.PREFIX_DECREMENT -> arg.add(Token(TokenType.MINUS, "--"))
//            UnaryExpr.Operator.LOGICAL_COMPLEMENT -> arg.add(Token(TokenType.NOT, "!"))
//            UnaryExpr.Operator.BITWISE_COMPLEMENT -> arg.add(Token(TokenType.BNOT, "~"))
//            UnaryExpr.Operator.POSTFIX_INCREMENT -> arg.add(Token(TokenType.PLUS, "++"))
//            UnaryExpr.Operator.POSTFIX_DECREMENT -> arg.add(Token(TokenType.MINUS, "--"))
//        }
//
//        if (n.isPrefix)
//            n.expression.accept(this, arg)
//    }
//
//    override fun visit(n: UnionType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: UnknownType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: VariableDeclarationExpr, arg: MutableList<Token>) {
//        n.variables.forEach { variable ->
//            n.annotations.accept(this, arg)
//            arg.add(Token(TokenType.VARIABLE, ""))
//            variable.type.accept(this, arg)
//            variable.name.accept(this, arg)
//
//            variable.initializer.ifPresent { initialiser ->
//                variable.name.accept(this, arg)
//                arg.add(Token(TokenType.ASSIGNMENT, "="))
//                initialiser.accept(this, arg)
//            }
//        }
//    }
//
//    override fun visit(n: VariableDeclarator, arg: MutableList<Token>) {
//        n.type.accept(this, arg)
//        n.name.accept(this, arg)
//    }
//
//    override fun visit(n: VoidType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: WhileStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.WHILE, "while"))
//        n.condition.accept(this, arg)
//
//        arg.add(Token(TokenType.LOOPBODY_START, "{"))
//        if (n.body.isBlockStmt)
//            n.body.asBlockStmt().statements.accept(this, arg)
//        else
//            n.body.accept(this, arg)
//        arg.add(Token(TokenType.LOOPBODY_END, "}"))
//    }
//
//    override fun visit(n: WildcardType, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.NAME, n.asString()))
//    }
//
//    override fun visit(n: ModuleDeclaration, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: ModuleRequiresDirective, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: ModuleExportsDirective, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: ModuleProvidesDirective, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: ModuleUsesDirective, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: ModuleOpensDirective, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: UnparsableStmt, arg: MutableList<Token>) {
//        arg.add(Token(TokenType.UNKNOWN, n.toString()))
//    }
//
//    override fun visit(n: ReceiverParameter, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: VarType, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun visit(n: Modifier, arg: MutableList<Token>) {
//
//        when (n.keyword) {
//            Modifier.Keyword.DEFAULT        -> arg.add(Token(TokenType.DEFAULT, "default"))
//            Modifier.Keyword.PUBLIC         -> arg.add(Token(TokenType.PUBLIC, "public"))
//            Modifier.Keyword.PROTECTED      -> arg.add(Token(TokenType.PROTECTED, "protected"))
//            Modifier.Keyword.PRIVATE        -> arg.add(Token(TokenType.PRIVATE, "private"))
//            Modifier.Keyword.ABSTRACT       -> arg.add(Token(TokenType.ABSTRACT, "abstract"))
//            Modifier.Keyword.STATIC         -> arg.add(Token(TokenType.STATIC, "static"))
//            Modifier.Keyword.FINAL          -> arg.add(Token(TokenType.FINAL, "final"))
//            Modifier.Keyword.TRANSIENT      -> arg.add(Token(TokenType.TRANSIENT, "transient"))
//            Modifier.Keyword.VOLATILE       -> arg.add(Token(TokenType.VOLATILE, "volatile"))
//            Modifier.Keyword.SYNCHRONIZED   -> arg.add(Token(TokenType.SYNCHRONIZED, "synchronised"))
//            Modifier.Keyword.NATIVE         -> arg.add(Token(TokenType.NATIVE, "native"))
//            Modifier.Keyword.STRICTFP       -> arg.add(Token(TokenType.STRICTFP, "strictfp"))
//            Modifier.Keyword.TRANSITIVE     -> arg.add(Token(TokenType.TRANSITIVE, "transitive"))
//
//            else -> throw IllegalStateException("Unknown modifier ${n}")
//        }
//    }
//
//    override fun visit(switchExpr: SwitchExpr, arg: MutableList<Token>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}
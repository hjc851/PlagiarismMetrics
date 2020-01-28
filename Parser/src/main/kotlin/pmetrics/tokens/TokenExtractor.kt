package pmetrics.tokens

import org.eclipse.jdt.core.dom.*

object TokenExtractor {
    fun extract(cu: CompilationUnit): List<Token> {
        val visitor = Visitor()
        cu.accept(visitor)
        return visitor.tokens
    }
    
    private class Visitor: ASTVisitor() {
        val tokens = mutableListOf<Token>()
        fun add(t: Token) = tokens.add(t)
        fun add(type: TokenType, lex: String) = add(Token(type, lex))

        fun List<Any?>.accept() {
            for (element in this) (element as ASTNode).accept(this@Visitor)
        }

        override fun visit(node: AnnotationTypeDeclaration): Boolean {
            node.modifiers().accept()
            add(TokenType.ANNOTATION, "@interface")
            node.name.accept(this)
            add(TokenType.TYPEBODY_START, "{")
            node.bodyDeclarations().accept()
            add(TokenType.TYPEBODY_END, "}")

            return false
        }

        override fun visit(node: AnnotationTypeMemberDeclaration): Boolean {
            node.modifiers().accept()
            node.type.accept(this)
            node.name.accept(this)
            add(TokenType.PARAMETERS_START, "(")
            add(TokenType.PARAMETERS_END, ")")
            node.default?.let {
                add(TokenType.ASSIGNMENT, "=")
                it.accept(this)
            }

            return false
        }

        override fun visit(node: AnonymousClassDeclaration): Boolean {
            add(TokenType.TYPEBODY_START, "{")
            node.bodyDeclarations().accept()
            add(TokenType.TYPEBODY_END, "}")

            return false
        }

        override fun visit(node: ArrayAccess): Boolean {
            node.array.accept(this)
            add(Token(TokenType.ARR_INDEX_L, "["))
            node.index.accept(this)
            add(Token(TokenType.ARR_INDEX_R, "]"))

            return false
        }

        override fun visit(node: ArrayCreation): Boolean {
            add(TokenType.NEW, "new")
            node.type.accept(this)

            for (dimension in node.dimensions()) {
                add(Token(TokenType.ARR_INDEX_L, "["))
                (dimension as ASTNode).accept(this)
                add(Token(TokenType.ARR_INDEX_R, "]"))
            }

            node.initializer?.let {
                add(TokenType.EQUALS, "=")
                it.accept(this)
            }

            return false
        }

        override fun visit(node: ArrayInitializer): Boolean {
            add(TokenType.ARRAYINIT_START, "{")
            node.expressions().accept()
            add(TokenType.ARRAYINIT_END, "}")

            return false
        }

        override fun visit(node: ArrayType): Boolean {
            add(TokenType.NAME, node.resolveBinding()?.qualifiedName ?: node.toString())

            return false
        }

        override fun visit(node: AssertStatement): Boolean {
            add(TokenType.ASSERT, "assert")
            add(TokenType.ARGUMENTS_START, "(")
            node.expression?.accept(this)
            node.message?.accept(this)
            add(TokenType.ARRAYINIT_END, ")")

            return false
        }

        override fun visit(node: Assignment): Boolean {
            node.leftHandSide.accept(this)

            when (node.operator) {
                Assignment.Operator.PLUS_ASSIGN -> add(TokenType.PLUS, "+")
                Assignment.Operator.MINUS_ASSIGN -> add(TokenType.MINUS, "-")
                Assignment.Operator.TIMES_ASSIGN -> add(TokenType.TIMES, "*")
                Assignment.Operator.DIVIDE_ASSIGN -> add(TokenType.DIVIDE, "/")
                Assignment.Operator.BIT_AND_ASSIGN -> add(TokenType.AND, "&&")
                Assignment.Operator.BIT_OR_ASSIGN -> add(TokenType.OR, "||")
                Assignment.Operator.BIT_XOR_ASSIGN -> add(TokenType.XOR, "^")
                Assignment.Operator.REMAINDER_ASSIGN -> add(TokenType.REMAINDER, "%")
                Assignment.Operator.LEFT_SHIFT_ASSIGN -> add(TokenType.LEFT_SHIFT, "<<")
                Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN -> add(TokenType.SIGNED_RIGHT_SHIFT, ">>")
                Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN -> add(TokenType.UNSIGNED_RIGHT_SHIFT, ">>>")
            }

            add(TokenType.EQUALS, "=")
            node.rightHandSide.accept(this)

            return false
        }

        override fun visit(node: Block): Boolean {
            add(TokenType.BLOCK_START, "{")
            node.statements().accept()
            add(TokenType.BLOCK_END, "}")

            return false
        }

        override fun visit(node: BooleanLiteral): Boolean {
            if (node.booleanValue()) add(TokenType.TRUE, "true")
            else add(TokenType.FALSE, "false")

            return false
        }

        override fun visit(node: BreakStatement): Boolean {
            add(TokenType.BREAK, "break")

            return false
        }

        override fun visit(node: CastExpression): Boolean {
            add(TokenType.CAST_START, "(")
            node.type.accept(this)
            add(TokenType.CAST_END, ")")
            node.expression.accept(this)

            return false
        }

        override fun visit(node: CatchClause): Boolean {
            add(TokenType.CATCH, "catch")
            add(TokenType.PARAMETERS_START, "(")
            node.exception.accept(this)
            add(TokenType.PARAMETERS_END, ")")
            node.body.accept(this)

            return false
        }

        override fun visit(node: CharacterLiteral): Boolean {
            add(TokenType.CHAR, node.escapedValue)

            return false
        }

        override fun visit(node: ClassInstanceCreation): Boolean {
            add(TokenType.NEW, "new")
            node.expression?.let {
                it.accept(this)
                add(TokenType.DOT, ".")
            }
            node.type.accept(this)

            if (node.typeArguments().isNotEmpty()) {
                add(TokenType.GENERICS_START, "<")
                node.typeArguments().accept()
                add(TokenType.GENERICS_END, ">")
            }

            add(TokenType.PARAMETERS_START, "(")
            node.arguments().accept()
            add(TokenType.PARAMETERS_END, ")")

            node.anonymousClassDeclaration?.accept(this)

            return false
        }

        override fun visit(node: CompilationUnit): Boolean {
            node.`package`?.accept(this)
            node.imports().accept()
            node.types().accept()

            return false
        }

        override fun visit(node: ConditionalExpression): Boolean {
            node.expression.accept(this)
            add(TokenType.QUESTION, "?")
            node.thenExpression.accept(this)
            add(TokenType.COLON, ":")
            node.elseExpression.accept(this)

            return false
        }

        override fun visit(node: ConstructorInvocation): Boolean {
            add(TokenType.THIS, "this")

            if (node.typeArguments().isNotEmpty()) {
                add(TokenType.GENERICS_START, "<")
                node.typeArguments().accept()
                add(TokenType.GENERICS_END, ">")
            }

            add(TokenType.PARAMETERS_START, "(")
            node.arguments().accept()
            add(TokenType.PARAMETERS_END, ")")

            return false
        }

        override fun visit(node: ContinueStatement): Boolean {
            add(TokenType.CONTINUE, "continue")

            return false
        }

        override fun visit(node: CreationReference): Boolean {
            node.type.accept(this)
            add(TokenType.COLON, ":")
            add(TokenType.COLON, ":")
            add(TokenType.NEW, "new")

            return false
        }

        override fun visit(node: Dimension): Boolean {
            add(TokenType.ARR_DIMENTION, "[]")

            return false
        }

        override fun visit(node: DoStatement): Boolean {
            add(TokenType.DOWHILE, "do")
            node.body.accept(this)
            add(TokenType.WHILE, "while")
            add(TokenType.CONDITION_START, "(")
            node.expression.accept(this)
            add(TokenType.CONDITION_END, ")")

            return false
        }

        override fun visit(node: EnhancedForStatement): Boolean {
            add(TokenType.FOREACH, "for")
            add(TokenType.LOOPPARAM_START, "(")
            node.parameter.accept(this)
            add(TokenType.COLON, ":")
            node.expression.accept(this)
            add(TokenType.LOOPPARAM_END, ")")
            node.body.accept(this)

            return false
        }

        override fun visit(node: EnumConstantDeclaration): Boolean {
            node.name.accept(this)
            if (node.arguments().isNotEmpty()) {
                add(TokenType.PARAMETERS_START, "(")
                node.arguments().accept()
                add(TokenType.PARAMETERS_END, ")")
            }

            node.anonymousClassDeclaration?.accept(this)

            return false
        }

        override fun visit(node: EnumDeclaration): Boolean {
            node.modifiers().accept()
            add(TokenType.ENUM, "enum")
            node.name.accept(this)
            if (node.superInterfaceTypes().isNotEmpty()) {
                add(TokenType.IMPLEMENTS, "implements")
                node.superInterfaceTypes().accept()
            }
            add(TokenType.TYPEBODY_START, "{")
            node.enumConstants().accept()
            node.bodyDeclarations().accept()
            add(TokenType.TYPEBODY_END, "}")

            return false
        }

        override fun visit(node: ExpressionMethodReference): Boolean {
            node.expression.accept(this)
            add(TokenType.COLON, ":")
            add(TokenType.COLON, ":")
            node.name.accept(this)

            return false
        }

        override fun visit(node: ExpressionStatement): Boolean {
            node.expression.accept(this)

            return false
        }

        override fun visit(node: FieldAccess): Boolean {
            node.expression.accept(this)
            add(TokenType.DOT, ".")
            node.name.accept(this)

            return false
        }

        override fun visit(node: FieldDeclaration): Boolean {
            node.modifiers().accept()
            node.type.accept(this)
            node.fragments().accept()

            return false
        }

        override fun visit(node: ForStatement): Boolean {
            add(TokenType.FOR, "for")
            add(TokenType.LOOPPARAM_START, "(")
            node.initializers()?.accept()
            node.expression?.accept(this)
            node.updaters()?.accept()
            add(TokenType.LOOPPARAM_END, ")")
            node.body.accept(this)

            return false
        }

        override fun visit(node: IfStatement): Boolean {
            add(TokenType.IF, "if")
            add(TokenType.CONDITION_START, "(")
            node.expression.accept(this)
            add(TokenType.CONDITION_END, ")")
            node.thenStatement.accept(this)

            node.elseStatement?.let {
                add(TokenType.ELSE, "else")
                it.accept(this)
            }

            return false
        }

        override fun visit(node: ImportDeclaration): Boolean {
            add(TokenType.IMPORT, "import")
            if (node.isStatic) add(TokenType.STATIC, "static")
            node.name.accept(this)
            if (node.isOnDemand) add(TokenType.ASTERIX, "*")

            return false
        }

        override fun visit(node: InfixExpression): Boolean {
            val token = when (node.operator) {
                InfixExpression.Operator.AND -> Token(TokenType.AND, "&")
                InfixExpression.Operator.OR -> Token(TokenType.OR, "|")
                InfixExpression.Operator.XOR -> Token(TokenType.XOR, "^")
                InfixExpression.Operator.CONDITIONAL_AND -> Token(TokenType.AND, "&&")
                InfixExpression.Operator.CONDITIONAL_OR -> Token(TokenType.OR, "||")

                InfixExpression.Operator.DIVIDE -> Token(TokenType.DIVIDE, "/")
                InfixExpression.Operator.TIMES -> Token(TokenType.TIMES, "*")
                InfixExpression.Operator.MINUS -> Token(TokenType.MINUS, "-")
                InfixExpression.Operator.PLUS -> Token(TokenType.PLUS, "+")
                InfixExpression.Operator.REMAINDER -> Token(TokenType.REMAINDER, "%")

                InfixExpression.Operator.EQUALS -> Token(TokenType.EQUALS, "==")
                InfixExpression.Operator.NOT_EQUALS -> Token(TokenType.NOT_EQUALS, "!=")
                InfixExpression.Operator.LESS_EQUALS -> Token(TokenType.LESS_EQUALS, "<=")
                InfixExpression.Operator.GREATER_EQUALS -> Token(TokenType.GREATER_EQUALS, ">=")
                InfixExpression.Operator.GREATER -> Token(TokenType.GREATER, ">")
                InfixExpression.Operator.LESS -> Token(TokenType.LESS, "<")

                InfixExpression.Operator.LEFT_SHIFT -> Token(TokenType.LEFT_SHIFT, "<<")
                InfixExpression.Operator.RIGHT_SHIFT_SIGNED -> Token(TokenType.SIGNED_RIGHT_SHIFT, ">>")
                InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED -> Token(TokenType.UNSIGNED_RIGHT_SHIFT, ">>>")

                else -> TODO()
            }

            node.leftOperand.accept(this)
            add(token)
            node.rightOperand.accept(this)

            node.extendedOperands().forEach { operand ->
                add(token)
                (operand as ASTNode).accept(this)
            }

            return false
        }

        override fun visit(node: Initializer): Boolean {
            node.modifiers().accept()
            node.body.accept(this)

            return false
        }

        override fun visit(node: InstanceofExpression): Boolean {
            node.leftOperand.accept(this)
            add(TokenType.INSTANCEOF, "instanceof")
            node.rightOperand.accept(this)

            return false
        }

        override fun visit(node: IntersectionType): Boolean {
            (node.types().first() as ASTNode).accept(this)

            node.types().drop(1).forEach { type ->
                add(TokenType.AND, "&")
                (type as ASTNode).accept(this)
            }

            return false
        }

        override fun visit(node: LabeledStatement): Boolean {
            node.label.accept(this)
            add(TokenType.COLON, ":")
            node.body.accept(this)

            return false
        }

        override fun visit(node: LambdaExpression): Boolean {
            if (node.hasParentheses()) add(TokenType.PARAMETERS_START, "(")
            node.parameters().accept()
            if (node.hasParentheses()) add(TokenType.PARAMETERS_END, ")")
            add(TokenType.ARROW, "->")
            node.body?.accept(this)

            return false
        }

        override fun visit(node: MethodDeclaration): Boolean {
            node.modifiers().accept()

            if (node.typeParameters().isNotEmpty()) {
                add(TokenType.LESS, "<")
                node.typeParameters().accept()
                add(TokenType.GREATER, ">")
            }

            node.returnType2?.accept(this)

            node.name.accept(this)
            add(TokenType.PARAMETERS_START, "(")
            node.parameters().accept()
            add(TokenType.PARAMETERS_END, ")")

            if (node.thrownExceptionTypes().isNotEmpty()) {
                add(TokenType.THROWS, "throws")
                node.thrownExceptionTypes().forEach { (it as ASTNode).accept(this) }
            }

            node.body?.accept(this)

            return false
        }

        override fun visit(node: MethodInvocation): Boolean {
            node.expression?.let {
                it.accept(this)
                add(TokenType.DOT, ".")
            }

            if (node.typeArguments().isNotEmpty()) {
                add(TokenType.LESS, "<")
                node.typeArguments().accept()
                add(TokenType.GREATER, ">")
            }

            node.name.accept(this)
            add(TokenType.ARGUMENTS_START, "(")
            node.arguments().forEach { (it as ASTNode).accept(this) }
            add(TokenType.ARGUMENTS_END, ")")

            return false
        }

        override fun visit(node: Modifier): Boolean {
            if (node.isAbstract) add(TokenType.ABSTRACT, "abstract")
            if (node.isDefault) add(TokenType.DEFAULT, "default")
            if (node.isFinal) add(TokenType.FINAL, "final")
            if (node.isNative) add(TokenType.NATIVE, "native")
            if (node.isPrivate) add(TokenType.PRIVATE, "private")
            if (node.isProtected) add(TokenType.PROTECTED, "protected")
            if (node.isPublic) add(TokenType.PUBLIC, "public")
            if (node.isStatic) add(TokenType.STATIC, "static")
            if (node.isStrictfp) add(TokenType.STRICTFP, "strictfp")
            if (node.isSynchronized) add(TokenType.SYNCHRONIZED, "synchronised")
            if (node.isTransient) add(TokenType.TRANSIENT, "transient")
            if (node.isVolatile) add(TokenType.VOLATILE, "volatile")

            return false
        }

        override fun visit(node: NameQualifiedType): Boolean {
            node.qualifier.accept(this)
            add(TokenType.DOT, ".")
            node.name.accept(this)

            return false
        }

        override fun visit(node: NullLiteral): Boolean {
            add(TokenType.NULL, "null")

            return false
        }

        override fun visit(node: NumberLiteral): Boolean {
            add(TokenType.NUMBER, node.token)

            return false
        }

        override fun visit(node: PackageDeclaration): Boolean {
            add(TokenType.PACKAGE, "package")
            node.name.accept(this)

            return false
        }

        override fun visit(node: ParameterizedType): Boolean {
            node.type.accept(this)

            add(TokenType.LESS, "<")
            node.typeArguments().accept()
            add(TokenType.GREATER, ">")

            return false
        }

        override fun visit(node: ParenthesizedExpression): Boolean {
            add(TokenType.PARAMETERS_START, "(")
            node.expression.accept(this)
            add(TokenType.PARAMETERS_END, ")")

            return false
        }

        override fun visit(node: PostfixExpression): Boolean {
            node.operand.accept(this)

            val token = when (node.operator) {
                PostfixExpression.Operator.INCREMENT -> Token(TokenType.PLUS, "+")
                PostfixExpression.Operator.DECREMENT -> Token(TokenType.MINUS, "-")

                else -> TODO()
            }
            add(token)

            return false
        }

        override fun visit(node: PrefixExpression): Boolean {

            val token = when (node.operator) {
                PrefixExpression.Operator.INCREMENT -> Token(TokenType.INCREMENT, "++")
                PrefixExpression.Operator.DECREMENT -> Token(TokenType.DECREMENT, "--")
                PrefixExpression.Operator.COMPLEMENT -> Token(TokenType.COMPLEMENT, "~")
                PrefixExpression.Operator.PLUS -> Token(TokenType.PLUS, "+")
                PrefixExpression.Operator.MINUS -> Token(TokenType.MINUS, "-")
                PrefixExpression.Operator.NOT -> Token(TokenType.NOT, "!")

                else -> TODO()
            }

            add(token)
            node.operand.accept(this)

            return false
        }

        override fun visit(node: PrimitiveType): Boolean {
            add(TokenType.NAME, node.toString())

            return false
        }

        override fun visit(node: QualifiedName): Boolean {
            add(TokenType.NAME, node.resolveBinding()?.name ?: node.toString())

            return false
        }

        override fun visit(node: QualifiedType): Boolean {
            add(TokenType.NAME, node.resolveBinding()?.qualifiedName ?: node.toString())

            return false
        }

        override fun visit(node: ReturnStatement): Boolean {
            add(TokenType.RETURN, "return")
            node.expression?.accept(this)

            return false
        }

        override fun visit(node: SimpleName): Boolean {
            add(TokenType.NAME, node.identifier)

            return false
        }

        override fun visit(node: SimpleType): Boolean {
            add(TokenType.NAME, node.resolveBinding()?.qualifiedName ?: node.toString())
            return false
        }

        override fun visit(node: SingleVariableDeclaration): Boolean {
            node.modifiers().accept()
            node.type.accept(this)

            if (node.isVarargs) add(TokenType.VARARG, "...")

            node.name.accept(this)

            node.initializer?.let {
                add(TokenType.EQUALS, "=")
                it.accept(this)
            }

            return false
        }

        override fun visit(node: StringLiteral): Boolean {
            add(TokenType.STRING, node.escapedValue)

            return false
        }

        override fun visit(node: SuperConstructorInvocation): Boolean {
            node.expression?.let {
                it.accept(this)
                add(TokenType.DOT, ".")
            }

            add(TokenType.SUPER, "super")

            if (node.typeArguments().isNotEmpty()) {
                add(TokenType.GENERICS_START, "<")
                node.typeArguments().accept()
                add(TokenType.GENERICS_END, ">")
            }

            add(TokenType.PARAMETERS_START, "(")
            node.arguments().accept()
            add(TokenType.PARAMETERS_END, ")")

            return false
        }

        override fun visit(node: SuperFieldAccess): Boolean {
            node.qualifier?.let {
                it.accept(this)
                add(TokenType.DOT, ".")
            }

            add(TokenType.SUPER, "super")
            add(TokenType.DOT, ".")
            node.name.accept(this)

            return false
        }

        override fun visit(node: SuperMethodInvocation): Boolean {
            node.qualifier?.let {
                it.accept(this)
                add(TokenType.DOT, ".")
            }

            add(TokenType.SUPER, "super")
            add(TokenType.DOT, ".")
            node.name.accept(this)

            if (node.typeArguments().isNotEmpty()) {
                add(TokenType.GENERICS_START, "<")
                node.typeArguments().accept()
                add(TokenType.GENERICS_END, ">")
            }

            add(TokenType.PARAMETERS_START, "(")
            node.arguments().accept()
            add(TokenType.PARAMETERS_END, ")")

            return false
        }

        override fun visit(node: SuperMethodReference): Boolean {
            add(TokenType.SUPER, "super")
            add(TokenType.COLON, ":")
            add(TokenType.COLON, ":")
            node.name.accept(this)

            return false
        }

        override fun visit(node: SwitchCase): Boolean {
            
            if (node.isDefault) {
                add(TokenType.DEFAULT, "default")
            } else {
                add(TokenType.CASE, "case")
                node.expression.accept(this)
            }
            
            add(TokenType.COLON, ":")
            
            return false
        }

        override fun visit(node: SwitchStatement): Boolean {
            add(TokenType.SWITCH, "switch")
            add(TokenType.CONDITION_START, "(")
            node.expression.accept(this)
            add(TokenType.CONDITION_END, ")")
            add(TokenType.BLOCK_START, "{")
            node.statements().accept()
            add(TokenType.BLOCK_END, "}")
            
            return false
        }

        override fun visit(node: SynchronizedStatement): Boolean {
            add(TokenType.SYNCHRONIZED, "synchronized")
            node.body.accept(this)

            return false
        }

        override fun visit(node: ThisExpression): Boolean {
            add(TokenType.THIS, "this")

            return false
        }

        override fun visit(node: ThrowStatement): Boolean {
            add(TokenType.THROW, "throw")
            node.expression.accept(this)
            
            return false
        }

        override fun visit(node: TryStatement): Boolean {
            add(TokenType.TRY, "try")
            if (node.resources().isNotEmpty()) {
                add(TokenType.PARAMETERS_START, "(")
                node.resources().accept()
                add(TokenType.PARAMETERS_END, ")")
            }
            node.body.accept(this)
            node.catchClauses().accept()
            node.finally?.accept(this)
            
            return false
        }

        override fun visit(node: TypeDeclaration): Boolean {
            node.modifiers().accept()
            if (node.isInterface) add(TokenType.INTERFACE, "interface")
            else add(TokenType.CLASS, "class")
            node.name.accept(this)

            if (node.typeParameters().isNotEmpty()) {
                add(TokenType.GENERICS_START, "<")
                node.typeParameters().accept()
                add(TokenType.GENERICS_END, ">")
            }

            node.superclassType?.let {
                add(TokenType.EXTENDS, "extends")
                it.accept(this)
            }

            if (node.superInterfaceTypes()?.isNotEmpty() ?: false) {
                add(TokenType.IMPLEMENTS, "implements")
                node.superInterfaceTypes().accept()
            }

            add(TokenType.TYPEBODY_START, "{")
            node.bodyDeclarations().accept()
            add(TokenType.TYPEBODY_END, "}")

            return false
        }

        override fun visit(node: TypeDeclarationStatement): Boolean {
            node.declaration.accept(this)

            return false
        }

        override fun visit(node: TypeLiteral): Boolean {
            node.type.accept(this)
            add(TokenType.DOT, ".")
            add(TokenType.CLASS, "class")

            return false
        }

        override fun visit(node: TypeMethodReference): Boolean {
            node.type.accept(this)
            add(TokenType.COLON, ":")
            add(TokenType.COLON, ":")
            node.name.accept(this)

            return false
        }

        override fun visit(node: TypeParameter): Boolean {
            node.name.accept(this)

            return false
        }

        override fun visit(node: UnionType): Boolean {
            for (i in 0 until node.types().size) {
                (node.types()[i] as ASTNode).accept(this)

                if (i < node.types().size-1)
                    add(TokenType.AND, "&")
            }
            return false
        }

        override fun visit(node: VariableDeclarationExpression): Boolean {
            node.modifiers().accept()
            node.type.accept(this)
            node.fragments().accept()

            return false
        }

        override fun visit(node: VariableDeclarationStatement): Boolean {
            node.modifiers().accept()
            node.type.accept(this)
            node.fragments().accept()

            return false
        }

        override fun visit(node: VariableDeclarationFragment): Boolean {
            node.name.accept(this)

            for (i in 0 until node.extraDimensions) add(TokenType.ARR_DIMENTION, "[]")

            node.initializer?.let {
                add(TokenType.EQUALS, "=")
                it.accept(this)
            }

            return false
        }

        override fun visit(node: WhileStatement): Boolean {
            add(TokenType.WHILE, "while")
            node.body.accept(this)

            return false
        }

        override fun visit(node: WildcardType): Boolean {
            add(TokenType.ASTERIX, "*")
            node.bound?.let { 
                if (node.isUpperBound) add(TokenType.EXTENDS, "extends")
                else add(TokenType.SUPER, "super")
                it.accept(this)    
            }
            
            return false
        }
    }
}